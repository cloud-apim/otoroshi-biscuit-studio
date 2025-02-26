package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitExtractorConfig, BiscuitForgeConfig, BiscuitKeyPair, BiscuitTokenForge}
import org.biscuitsec.biscuit.crypto.{KeyPair, PublicKey}
import otoroshi.models.{ApiKey, EntityLocation, RouteIdentifier}
import otoroshi.next.models._
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import play.api.libs.json.Json
import reactor.core.publisher.Mono
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitApiKeyBridgePlugin

import java.util.UUID
import scala.concurrent.duration.DurationInt

class BiscuitApiKeyBridgeSuite extends BiscuitStudioOneOtoroshiServerPerSuite {

  test("should bridge an apikey from a biscuit token") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val (tport, _) = createTestServerWithRoutes("test-apikey-biscuit-bridge", routes => routes.get("/api", (req, response) => {
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
          plugin = s"cp:${classOf[BiscuitApiKeyBridgePlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "keypair_ref" -> keypair.id,
            "extractor_type" -> "header",
            "extractor_name" -> "Authorization"
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

    val goodToken = BiscuitExtractorConfig.replaceHeader(respGoodToken.json.at("token").get.asString)
    assert(goodToken.nonEmpty, s"token is empty")

    val publicKeyFormatted = new PublicKey(biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519, keypair.pubKey)

    val encodedGoodBiscuit =  org.biscuitsec.biscuit.token.Biscuit.from_b64url(goodToken, publicKeyFormatted)

    assertEquals(encodedGoodBiscuit.authorizer().facts().size(), forge1.config.facts.length, s"encodedGoodBiscuit doesn't contain all facts")
    assertEquals(encodedGoodBiscuit.authorizer().checks().size(), forge1.config.checks.length, s"encodedGoodBiscuit doesn't contain all checks")

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

    val badToken = BiscuitExtractorConfig.replaceHeader(respBadToken.json.at("token").get.asString)
    assert(badToken.nonEmpty, s"token is empty")

    val encodedBadToken = Biscuit.from_b64url(badToken, publicKeyFormatted)

    assertEquals(encodedBadToken.authorizer().facts().size(), forge2.config.facts.length, s"encodedBadToken doesn't contain all facts")
    assertEquals(encodedBadToken.authorizer().checks().size(), forge2.config.checks.length, s"encodedBadToken doesn't contain all checks")

    await(3.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test                                                          ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val res = client.call("GET", s"http://test.oto.tools:${port}/api", Map("Authorization" -> s"Biscuit: ${goodToken}"), None).awaitf(30.seconds)
    val resWrongToken = client.call("GET", s"http://test.oto.tools:${port}/api", Map("Authorization" -> s"Biscuit: ${badToken}"), None).awaitf(30.seconds)

    assertEquals(res.status, 200, "status should be 200")
    assertEquals(resWrongToken.status, 401, "status should be unauthorized 401")
    assert(resWrongToken.json.at("error").isDefined, "body error should be defined")
    assertEquals(resWrongToken.json.at("error").asString, "unauthorized", "error should be 'unauthorized'")
    assert(resWrongToken.json.at("error_description").isDefined, "error_description should be defined")
    assertEquals(resWrongToken.json.at("error_description").asString, "Api Key (based on biscuit fact 'client_id') doesn't exist", "error_description should be 'Api Key (based on biscuit fact 'client_id') doesn't exist'")

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
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").deleteEntity(forge1)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").deleteEntity(forge2)
    client.forEntity("apim.otoroshi.io", "v1", "apikeys").deleteEntity(apikey)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeApi)
    await(2.seconds)
  }
}