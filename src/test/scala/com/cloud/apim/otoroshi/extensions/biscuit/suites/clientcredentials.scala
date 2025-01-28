package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitKeyPair, BiscuitTokenForge, BiscuitVerifier, VerifierConfig}
import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitForgeConfig
import org.biscuitsec.biscuit.crypto.KeyPair
import otoroshi.models.{ApiKey, EntityLocation, RouteIdentifier}
import otoroshi.next.models._
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.{BiscuitTokenValidator, ClientCredentialBiscuitTokenEndpoint}
import play.api.libs.json.Json
import reactor.core.publisher.Mono

import java.util.UUID
import scala.concurrent.duration.DurationInt

class ClientcredentialsSuite extends BiscuitStudioOneOtoroshiServerPerSuite {

  test("should be able to exchange a biscuit access_token against apikey") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val (tport, _) = createTestServerWithRoutes("test", routes => routes.get("/api", (req, response) => {
      response
        .status(200)
        .addHeader("Content-Type", "application/json")
        .sendString(Mono.just(
          s"""{
             |  "foo": "foobar",
             |}""".stripMargin))
    }))

    val clientId = IdGenerator.token(16)
    val biscuitKeyPair = new KeyPair()
    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex
    )
    val forge = BiscuitTokenForge(
      id = IdGenerator.namedId("biscuit-forge", otoroshi.env),
      name = "New biscuit token",
      description = "New biscuit token",
      keypairRef = keypair.id,
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitForgeConfig(
        checks = Seq.empty,
        facts = Seq.empty,
        resources = Seq.empty,
        rules = Seq.empty
      ),
      remoteFactsLoaderRef = None
    )
    val validator = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      name = "New biscuit verifier",
      description = "New biscuit verifier",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = VerifierConfig(
        checks = Seq(
          s"""check if client_id("${clientId}")""",
          s"""check if aud("http://test.oto.tools:${port}")""",
        ),
        facts = Seq.empty,
        resources = Seq.empty,
        rules = Seq.empty,
        policies = Seq(
          s"""allow if true""",
        ),
        revokedIds = Seq.empty,
      ).some
    )
    val routeCCEndpoint = NgRoute(
      location = EntityLocation.default,
      id = UUID.randomUUID().toString,
      name = "test route",
      description = "test route",
      tags = Seq.empty,
      metadata = Map.empty,
      enabled = true,
      debugFlow = false,
      capture = false,
      exportReporting = false,
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath("test.oto.tools/token"))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(NgPluginInstance(
        plugin = s"cp:${classOf[ClientCredentialBiscuitTokenEndpoint].getName}",
        config = NgPluginInstanceConfig(Json.obj(
          "forge_ref" -> forge.id
        ))
      )))
    )
    val routeApi = NgRoute(
      location = EntityLocation.default,
      id = UUID.randomUUID().toString,
      name = "test route",
      description = "test route",
      tags = Seq.empty,
      metadata = Map.empty,
      enabled = true,
      debugFlow = false,
      capture = false,
      exportReporting = false,
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath("test.oto.tools/api")), stripPath = false),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.parse(s"http://localhost:${tport}"))),
      plugins = NgPlugins(Seq(NgPluginInstance(
        plugin = s"cp:${classOf[BiscuitTokenValidator].getName}",
        config = NgPluginInstanceConfig(Json.obj(
          "verifier_ref" -> validator.id
        ))
      )))
    )
    val apikey = ApiKey(
      clientId = clientId,
      clientSecret = IdGenerator.token(16),
      clientName = "test",
      authorizedEntities = Seq(RouteIdentifier(routeApi.id)),
    )

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "token-forges").upsertEntity(forge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(validator)
    client.forEntity("apim.otoroshi.io", "v1", "apikeys").upsertEntity(apikey)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeCCEndpoint)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeApi)
    await(2.seconds)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test                                                          ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val res0 = client.call("POST", s"http://test.oto.tools:${port}/token", Map("Content-Type" -> s"application/json"), Some(Json.obj(
      "grant_type" -> "client_credentials",
      "client_id" -> apikey.clientId,
      "client_secret" -> apikey.clientSecret,
      "bearer_kind" -> "biscuit",
      "aud" -> s"http://test.oto.tools:${port}",
    ))).awaitf(30.seconds)
    assertEquals(res0.status, 200, "status should be 200")
    val token = res0.json.select("access_token").asOptString.getOrElse("--")
    assertNotEquals(token, "--", "should not be --")
    val res = client.call("GET", s"http://test.oto.tools:${port}/api", Map("Authorization" -> s"Bearer ${token}"), None).awaitf(30.seconds)
    assertEquals(res.status, 200, "status should be 200")
    assert(res.body.contains("foobar"), "body contains foobar")
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "token-forges").deleteEntity(forge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").deleteEntity(validator)
    client.forEntity("apim.otoroshi.io", "v1", "apikeys").deleteEntity(apikey)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeCCEndpoint)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeApi)
    await(2.seconds)
  }

}
