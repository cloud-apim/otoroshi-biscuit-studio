package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import org.biscuitsec.biscuit.crypto.KeyPair
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.models.EntityLocation
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.duration.DurationInt

class TestBiscuitUserExtractorPlugin extends BiscuitStudioOneOtoroshiServerPerSuite {

  test("should be able to extract an user from a biscuit token and put it into context") {
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

    val publicKeyFormatted = keypair.getPubKey

    val tokenUserId = UUID.randomUUID()
    val username = "otoroshi-biscuit-studio-test"

    val forge = BiscuitTokenForge(
      id = IdGenerator.namedId("biscuit-forge", otoroshi.env),
      name = "New biscuit token",
      description = "New biscuit token",
      keypairRef = keypair.id,
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitForgeConfig(
        facts = Seq(
          "name(\"otoroshi-biscuit-studio-test\")",
          s"""user_id("${tokenUserId.toString}")"""
        ),
      )
    )

    val forgeGoodToken = BiscuitTokenForge(
      id = IdGenerator.namedId("biscuit-forge", otoroshi.env),
      name = "New biscuit token",
      description = "New biscuit token",
      keypairRef = keypair.id,
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitForgeConfig(
        facts = Seq(
          s"""username("${username}")""",
          s"""user_id("${tokenUserId.toString}")"""
        ),
      )
    )

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test BAD biscuit creation from forge                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val resp2 = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/_generate", Map("Content-Type" -> s"application/json"), Some(Json.obj(
      "config" -> forge.config.json,
      "keypair_ref" -> keypair.id
    ))).awaitf(5.seconds)
    assertEquals(resp2.status, 200, s"verifier route did not respond with 200")
    assert(resp2.json.at("done").isDefined, s"generation of the token failed")
    assert(resp2.json.at("done").asBoolean, s"token has not been well generated")
    assert(resp2.json.at("token").isDefined, s"token has not been generated")

    val token = BiscuitExtractorConfig.replaceHeader(resp2.json.at("token").get.asString)
    assert(token.nonEmpty, s"token is empty")

    val encodedBiscuit = Biscuit.from_b64url(token, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().facts().size(), forge.config.facts.length + forge.config.resources.length, s"token doesn't contain all facts")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test GOOD biscuit creation from forge                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respGoodToken = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/_generate", Map("Content-Type" -> s"application/json"), Some(Json.obj(
      "config" -> forgeGoodToken.config.json,
      "keypair_ref" -> keypair.id
    ))).awaitf(5.seconds)
    assertEquals(respGoodToken.status, 200, s"verifier route did not respond with 200")
    assert(respGoodToken.json.at("done").isDefined, s"generation of the token failed")
    assert(respGoodToken.json.at("done").asBoolean, s"token has not been well generated")
    assert(respGoodToken.json.at("token").isDefined, s"token has not been generated")

    val goodToken = BiscuitExtractorConfig.replaceHeader(respGoodToken.json.at("token").get.asString)
    assert(goodToken.nonEmpty, s"token is empty")

    val encodedGoodBiscuit = Biscuit.from_b64url(goodToken, publicKeyFormatted)
    assertEquals(encodedGoodBiscuit.authorizer().facts().size(), forge.config.facts.length + forge.config.resources.length, s"token doesn't contain all facts")


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                      create the route with the 'Biscuit User Extractor' plugin                 ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val routeId = s"route_${UUID.randomUUID().toString}"
    val routeDomain = "user-extractor.oto.tools"

    val routeWithAttenuator = client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeId, Json.parse(
      s"""{
         |  "id": "${routeId}",
         |  "name": "biscuit-attenuator",
         |  "frontend": {
         |    "domains": [
         |      "${routeDomain}"
         |    ]
         |  },
         |  "backend": {
         |    "targets": [
         |      {
         |        "id": "target_1",
         |        "hostname": "request.otoroshi.io",
         |        "port": 443,
         |        "tls": true
         |      }
         |    ],
         |    "root": "/",
         |    "rewrite": false,
         |    "load_balancing": {
         |      "type": "RoundRobin"
         |    }
         |  },
         |   "backend_ref": null,
         |  "plugins": [
         |    {
         |      "enabled": true,
         |      "debug": false,
         |      "plugin": "cp:otoroshi.next.plugins.OverrideHost",
         |      "include": [],
         |      "exclude": [],
         |      "config": {},
         |      "bound_listeners": [],
         |      "plugin_index": {
         |        "transform_request": 0
         |      }
         |    },
         |    {
         |      "plugin_index": {},
         |      "plugin": "cp:otoroshi.next.plugins.UserProfileEndpoint",
         |      "enabled": true,
         |      "debug": false,
         |      "include": [],
         |      "exclude": [],
         |      "bound_listeners": [],
         |      "config": {}
         |    },
         |    {
         |      "enabled": true,
         |      "debug": false,
         |      "plugin": "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitUserExtractor",
         |      "include": [],
         |      "exclude": [],
         |      "config": {
         |        "keypair_ref": "${keypair.id}",
         |        "username_key": "username"
         |      },
         |      "bound_listeners": [],
         |      "plugin_index": {
         |        "validate_access": 0
         |      }
         |    }
         |  ]
         |}""".stripMargin)).awaitf(30.seconds)
    assert(routeWithAttenuator.created, s"attenuator route has not been created")
    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                    call the route with the  BAD token                               ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respWithBadToken = client.call("GET", s"http://${routeDomain}:${port}", Map("Authorization" -> token), None).awaitf(30.seconds)

    assertEquals(respWithBadToken.status, 401, s"plugin should return an error 401")
    assert(respWithBadToken.json.at("error").isDefined, s"error should be defined for bad token")
    assertEquals(respWithBadToken.json.at("error").asString, "unauthorized", s"error should be 'unauthorized'")

    assert(respWithBadToken.json.at("error_description").isDefined, s"'error_description' should be defined for bad token")
    assertEquals(respWithBadToken.json.at("error_description").asString, "Bad user extraction, user id or username not valid", s"error_description should be 'Bad user extraction, user id or username not valid'")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                    call the route with the GOOD token                               ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respWithGoodToken = client.call("GET", s"http://${routeDomain}:${port}", Map("Authorization" -> goodToken), None).awaitf(30.seconds)

    assertEquals(respWithGoodToken.status, 200, s"verifier route with good token in headers did not respond with 200")
    assert(respWithGoodToken.json.at("name").isDefined, s"should get 'name' from body response")
    assertEquals(respWithGoodToken.json.at("name").asString, tokenUserId.toString, s"should get 'name' from body response")

    assert(respWithGoodToken.json.at("email").isDefined, s"should get 'email' from body response")
    assertEquals(respWithGoodToken.json.at("email").asString, username, s"should get 'email' from body response")

    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeId)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(keypair)
    await(5.seconds)
  }
}
