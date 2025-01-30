package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitKeyPair, BiscuitTokenForge, BiscuitVerifier, VerifierConfig}
import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitForgeConfig, BiscuitUtils}
import org.biscuitsec.biscuit.crypto.{KeyPair, PublicKey}
import otoroshi.models.{ApiKey, EntityLocation, RouteIdentifier}
import otoroshi.next.models._
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenValidator
import play.api.libs.json.Json
import reactor.core.publisher.Mono
import org.biscuitsec.biscuit.token.Biscuit

import java.util.UUID
import scala.concurrent.duration.DurationInt

class BiscuitApiKeyBridge extends BiscuitStudioOneOtoroshiServerPerSuite {

  test("create a bridge between apikey and biscuit token") {
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
    val forge1 = BiscuitTokenForge(
      id = IdGenerator.namedId("biscuit-forge", otoroshi.env),
      name = "New biscuit token",
      description = "New biscuit token",
      keypairRef = keypair.id,
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitForgeConfig(
        facts = Seq(
          s"""client_id("${clientId}")"""
        ),
        checks = Seq(
          s"""check if client_id("${clientId}")"""
        ),
        resources = Seq.empty,
        rules = Seq.empty
      ),
      remoteFactsLoaderRef = None
    )


    // forge2 : forge to create wrong token with unexisting apikey
    val forge2 = BiscuitTokenForge(
      id = IdGenerator.namedId("biscuit-forge", otoroshi.env),
      name = "New biscuit token",
      description = "New biscuit token",
      keypairRef = keypair.id,
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitForgeConfig(
        facts = Seq(
          s"""client_id("${IdGenerator.token(16)}")""",
        ),
        checks = Seq.empty,
        resources = Seq.empty,
        rules = Seq.empty
      ),
      remoteFactsLoaderRef = None
    )

    val validator1 = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      name = "New biscuit verifier",
      description = "New biscuit verifier",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = VerifierConfig(
        checks = Seq.empty,
        facts = Seq.empty,
        resources = Seq.empty,
        rules = Seq.empty,
        policies = Seq(
          s"""allow if true""",
        ),
        revokedIds = Seq.empty,
      ).some
    )

    val validator2 = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      name = "New biscuit verifier",
      description = "New biscuit verifier",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = VerifierConfig(
        checks = Seq.empty,
        facts = Seq.empty,
        resources = Seq.empty,
        rules = Seq.empty,
        policies = Seq(
          s"""allow if true"""
        ),
        revokedIds = Seq.empty,
      ).some
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
      plugins = NgPlugins(Seq(
        NgPluginInstance(
          plugin = s"cp:${classOf[BiscuitTokenValidator].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "verifier_ref" -> validator1.id
          ))
        ),
        NgPluginInstance(
          plugin = s"cp:otoroshi.next.plugins.ApikeyCalls",
          config = NgPluginInstanceConfig(Json.obj(
            "extractors" -> Json.obj(
              "basic" -> Json.obj(
                "enabled" -> true,
                "header_name" -> null,
                "query_name" -> null
              ),
              "custom_headers" -> Json.obj(
                "enabled" -> true,
                "client_id_header_name" -> null,
                "client_secret_header_name" -> null
              ),
              "client_id" -> Json.obj(
                "enabled" -> true,
                "header_name" -> null,
                "query_name" -> null
              ),
              "jwt" -> Json.obj(
                "enabled" -> true,
                "secret_signed" -> true,
                "keypair_signed" -> true,
                "include_request_attrs" -> false
              )
            ),
            "routing" -> Json.obj("enabled" -> false),
            "validate" -> true,
            "mandatory" -> true,
            "pass_with_user" -> false,
            "update_quotas" -> true
          ))
        ),
        NgPluginInstance(
          plugin = s"cp:otoroshi.next.plugins.ApikeyQuotas",
          config = NgPluginInstanceConfig(Json.obj())
        )
      ))
    )
    val apikey = ApiKey(
      clientId = clientId,
      clientSecret = IdGenerator.token(16),
      clientName = "test",
      authorizedEntities = Seq(RouteIdentifier(routeApi.id)),
    )

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge1)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge2)

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(validator1)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(validator2)
    client.forEntity("apim.otoroshi.io", "v1", "apikeys").upsertEntity(apikey)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeApi)

    await(2.seconds)


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  generated good token                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val respGoodToken = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${forge1.id}/_generate",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), None).awaitf(10.seconds)
    assertEquals(respGoodToken.status, 200, s"verifier route did not respond with 200")
    assert(respGoodToken.json.at("token").isDefined, s"token not generated")

    val goodToken = BiscuitUtils.replaceHeader(respGoodToken.json.at("token").get.asString)
    assert(goodToken.nonEmpty, s"token is empty")

    val publicKeyFormatted = new PublicKey(biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519, keypair.pubKey)

    val encodedGoodBiscuit =  org.biscuitsec.biscuit.token.Biscuit.from_b64url(goodToken, publicKeyFormatted)

    assertEquals(encodedGoodBiscuit.authorizer().facts().size(), forge1.config.facts.length + validator1.config.get.facts.size, s"encodedGoodBiscuit doesn't contain all facts")
    assertEquals(encodedGoodBiscuit.authorizer().checks().size(), forge1.config.checks.length + validator1.config.get.checks.size, s"encodedGoodBiscuit doesn't contain all checks")

    await(3.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  generated wrong token                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val respBadToken = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${forge2.id}/_generate",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), None).awaitf(10.seconds)
    assertEquals(respBadToken.status, 200, s"verifier route did not respond with 200")
    assert(respBadToken.json.at("token").isDefined, s"token not generated")

    val badToken = BiscuitUtils.replaceHeader(respBadToken.json.at("token").get.asString)
    assert(badToken.nonEmpty, s"token is empty")

    val encodedBadToken = Biscuit.from_b64url(badToken, publicKeyFormatted)

    assertEquals(encodedBadToken.authorizer().facts().size(), forge2.config.facts.length + validator2.config.get.facts.size, s"encodedBadToken doesn't contain all facts")
    assertEquals(encodedBadToken.authorizer().checks().size(), forge2.config.checks.length + validator2.config.get.checks.size, s"encodedBadToken doesn't contain all checks")

    await(3.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test                                                          ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val res = client.call("GET", s"http://test.oto.tools:${port}/api", Map("Authorization" -> s"Biscuit: ${goodToken}"), None).awaitf(30.seconds)
    val resWrongToken = client.call("GET", s"http://test.oto.tools:${port}/api", Map("Authorization" -> s"Biscuit: ${badToken}"), None).awaitf(30.seconds)

    assertEquals(res.status, 200, "status should be 200")
    assertEquals(resWrongToken.status, 500, "status should be 500")
    assert(resWrongToken.json.at("error").isDefined, "status should be 500")
    assertEquals(resWrongToken.json.at("error").asString, "Api Key (based on biscuit fact 'client_id') doesn't exist", "status should be 500")


    val quotasRes = client.call("GET", s"http://otoroshi-api.oto.tools:${port}/api/apikeys/${apikey.clientId}/quotas", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), None).awaitf(30.seconds)

    // CHECK QUOTAS
    assert(quotasRes.json.at("currentCallsPerDay").isDefined, "status should be defined")
    assertEquals(quotasRes.json.at("currentCallsPerDay").asInt, 1, "should be 1 quota consumed")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").deleteEntity(validator1)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").deleteEntity(validator2)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").deleteEntity(forge1)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").deleteEntity(forge2)
    client.forEntity("apim.otoroshi.io", "v1", "apikeys").deleteEntity(apikey)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeApi)
    await(2.seconds)
  }
}