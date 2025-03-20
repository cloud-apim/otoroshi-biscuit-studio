package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import org.biscuitsec.biscuit.crypto.KeyPair
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.models.EntityLocation
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import play.api.libs.json.Json
import reactor.core.publisher.Mono

import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._

class BackofficeRoutesSuite extends BiscuitStudioOneOtoroshiServerPerSuite {

  test("should be able to generate a keypair") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                       test endpoint                                            ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val resp = client.call("GET", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/keypairs/_generate",
      Map(
        "Content-Type" -> s"application/json"
      ), None).awaitf(5.seconds)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    assert(resp.json.at("done").isDefined, s"'done' field should be defined")
    assert(resp.json.at("done").asBoolean, s"request 'done' status should be true")
    assert(resp.json.at("pubKey").isDefined, "'pubKey' should be defined")
    assert(resp.json.at("privKey").isDefined, "'privKey' should be defined")
  }

  test("should be able to generate a token with public and private keys") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                       test endpoint                                            ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val keypair = new KeyPair()

    val tokenConfig = BiscuitForgeConfig(
      facts = Seq(
        "name(\"otoroshi-biscuit-studio-test\")",
        "user(\"biscuit-test-user\")",
        "role(\"user\")"
      ),
      checks = Seq(
        "check if user(\"biscuit-test-user\")",
        "check if role(\"user\")"
      )
    )

    val resp = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/_generate",
      Map(
        "Content-Type" -> s"application/json"
      ),
      Some(
        Json.obj(
          "pubKey" -> keypair.public_key().toHex,
          "privKey" -> keypair.toHex,
          "config" -> tokenConfig.json
        )
      )
    ).awaitf(5.seconds)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    assert(resp.json.at("done").isDefined, s"'done' field should be defined")
    assert(resp.json.at("done").asBoolean, s"request 'done' status should be true")
    assert(resp.json.at("pubKey").isDefined, "'pubKey' should be defined")
    assertEquals(resp.json.at("pubKey").asString, keypair.public_key().toHex, "public key response should match to given public key")
    assert(resp.json.at("token").isDefined, "'token' should be defined")

    assert(resp.json.at("token").isDefined, "token should be successfully generated")

    val genToken = resp.json.at("token").asString
    val encodedBiscuit = Biscuit.from_b64url(genToken, keypair.public_key())

    assertEquals(encodedBiscuit.authorizer().facts().size(), tokenConfig.facts.length, s"generated token doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, tokenConfig.checks.length, s"generated token doesn't contain all checks")
  }

  test("should be able to generate a token with a keypair_ref") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                       test endpoint                                            ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
    val tokenConfig = BiscuitForgeConfig(
      facts = Seq(
        "name(\"otoroshi-biscuit-studio-test\")",
        "user(\"biscuit-test-user\")",
        "role(\"user\")"
      ),
      checks = Seq(
        "check if user(\"biscuit-test-user\")",
        "check if role(\"user\")"
      )
    )

    /// Create keypair entity
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    await(5.seconds)

    val resp = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/_generate",
      Map(
        "Content-Type" -> s"application/json"
      ),
      Some(
        Json.obj(
          "keypair_ref" -> keypair.id,
          "config" -> tokenConfig.json
        )
      )
    ).awaitf(5.seconds)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    assert(resp.json.at("done").isDefined, s"'done' field should be defined")
    assert(resp.json.at("done").asBoolean, s"request 'done' status should be true")
    assert(resp.json.at("pubKey").isDefined, "'pubKey' should be defined")
    assertEquals(resp.json.at("pubKey").asString, biscuitKeyPair.public_key().toHex, "public key response should match to given public key")
    assert(resp.json.at("token").isDefined, "'token' should be defined")

    assert(resp.json.at("token").isDefined, "token should be successfully generated")

    val genToken = resp.json.at("token").asString
    val encodedBiscuit = Biscuit.from_b64url(genToken, biscuitKeyPair.public_key())

    assertEquals(encodedBiscuit.authorizer().facts().size(), tokenConfig.facts.length, s"generated token doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, tokenConfig.checks.length, s"generated token doesn't contain all checks")
  }

  test("should be able to generate a token with a keypair_ref AND remoteFactsLoaderRef") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                       test endpoint                                            ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
    val tokenConfig = BiscuitForgeConfig(
      facts = Seq(
        "name(\"otoroshi-biscuit-studio-test\")",
        "user(\"biscuit-test-user\")",
        "role(\"user\")"
      ),
      checks = Seq(
        "check if operation(\"write\")",
      )
    )

    val domainAPIPrefix = "test-api"
    val routeAPIPath = "/api/facts"

    val (tport, _) = createTestServerWithRoutes(domainAPIPrefix, routes => routes.post(routeAPIPath, (req, response) => {
      req.receive().retain().asString().flatMap { body =>
        response
          .status(200)
          .addHeader("Content-Type", "application/json")
          .sendString(Mono.just(
            s"""{
               |"facts": [
               |{
               |  "name": "role",
               |  "value": "dev"
               |},
               |{
               |  "name": "user",
               |  "value": "biscuit-demo"
               |},
               |{
               |  "name": "version",
               |  "value": "dev"
               |},
               |{
               |  "name": "operation",
               |  "value": "write"
               |}
               |]
               |}""".stripMargin))
      }
    }))

    val fullDomain = s"http://${domainAPIPrefix}.oto.tools:${tport}${routeAPIPath}"

    val respRemoteAPI = client.call("POST", fullDomain, Map("Content-Type" -> "application/json"), Some(Json.obj("foo" -> "bar"))).awaitf(5.seconds)
    assertEquals(respRemoteAPI.status, 200, s"remote facts API did not respond with 200")
    assert(respRemoteAPI.json.at("facts").isDefined, s"forge facts array is not defined")

    val forgeFactsArr = respRemoteAPI.json.at("facts").as[List[Map[String, String]]]
    assertEquals(forgeFactsArr.length, 4, s"forge facts array length doesn't match")

    val rfl = RemoteFactsLoader(
      id = IdGenerator.namedId("biscuit-remote-facts", otoroshi.env),
      name = "New biscuit remote facts loader",
      description = "New biscuit remote facts loader",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitRemoteFactsConfig(
        apiUrl = fullDomain,
        headers = Map(
          "Content-Type" -> "application/json"
        )
      )
    )

    /// Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-remote-facts").upsertEntity(rfl)
    await(5.seconds)

    val resp = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/_generate",
      Map(
        "Content-Type" -> s"application/json"
      ),
      Some(
        Json.obj(
          "keypair_ref" -> keypair.id,
          "remoteFactsLoaderRef" -> rfl.id,
          "config" -> tokenConfig.json
        )
      )
    ).awaitf(5.seconds)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")

    assert(resp.json.at("done").isDefined, s"'done' field should be defined")
    assert(resp.json.at("done").asBoolean, s"request 'done' status should be true")
    assert(resp.json.at("pubKey").isDefined, "'pubKey' should be defined")
    assertEquals(resp.json.at("pubKey").asString, biscuitKeyPair.public_key().toHex, "public key response should match to given public key")
    assert(resp.json.at("token").isDefined, "'token' should be defined")

    assert(resp.json.at("token").isDefined, "token should be successfully generated")

    val genToken = resp.json.at("token").asString
    val encodedBiscuit = Biscuit.from_b64url(genToken, biscuitKeyPair.public_key())

    assertEquals(encodedBiscuit.authorizer().facts().size(), tokenConfig.facts.length + 4, s"generated token doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, tokenConfig.checks.length, s"generated token doesn't contain all checks")
  }

  test("should be able to verify a token with a forge_ref and body config") {
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
        facts = Seq(
          "name(\"otoroshi-biscuit-studio-test\")",
          "user(\"biscuit-test-user\")",
          "role(\"user\")"
        ),
        checks = Seq(
          "check if server(\"biscuit-server-test\")",
          "check if operation(\"read\")"
        )
      )
    )

    val verifierConfig = VerifierConfig(
      checks = Seq(
        "check if name(\"otoroshi-biscuit-studio-test\")",
        "check if user(\"biscuit-test-user\")",
        "check if role(\"user\")"
      ),
      facts = Seq(
        "server(\"biscuit-server-test\")",
        "operation(\"read\")"
      )
    )

    /// Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(5.seconds)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test Good biscuit verifier                                    ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val goodVerifier = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/verifiers/_test", Map(
      "Content-Type" -> s"application/json"
    ), Some(Json.obj(
      "forge_ref" -> forge.id,
      "keypair_ref" -> keypair.id,
      "config" -> verifierConfig.json
    ))).awaitf(5.seconds)

    assert(goodVerifier.json.at("status").isDefined, "status for good verifier should be defined")
    assert(goodVerifier.json.at("message").isDefined, "message for good verifier should be defined")
    assertEquals(goodVerifier.json.at("status").asString, "success", "status message should be success")
    assertEquals(goodVerifier.json.at("message").asString, "Checked successfully", "message should be 'Checked successfully'")
  }

  test("should be able to test remote facts") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                       test endpoint                                            ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val domainAPIPrefix = "test-api"
    val routeAPIPath = "/api/facts"

    val (tport, _) = createTestServerWithRoutes(domainAPIPrefix, routes => routes.post(routeAPIPath, (req, response) => {
      req.receive().retain().asString().flatMap { body =>
        response
          .status(200)
          .addHeader("Content-Type", "application/json")
          .sendString(Mono.just(
            s"""{
               |"facts": [
               |{
               |  "name": "role",
               |  "value": "dev"
               |},
               |{
               |  "name": "user",
               |  "value": "biscuit-demo"
               |},
               |{
               |  "name": "version",
               |  "value": "dev"
               |},
               |{
               |  "name": "operation",
               |  "value": "write"
               |}
               |]
               |}""".stripMargin))
      }
    }))

    val fullDomain = s"http://${domainAPIPrefix}.oto.tools:${tport}${routeAPIPath}"

    val respRemoteAPI = client.call("POST", fullDomain, Map("Content-Type" -> "application/json"), Some(Json.obj("foo" -> "bar"))).awaitf(5.seconds)
    assertEquals(respRemoteAPI.status, 200, s"remote facts API did not respond with 200")
    assert(respRemoteAPI.json.at("facts").isDefined, s"forge facts array is not defined")

    val forgeFactsArr = respRemoteAPI.json.at("facts").as[List[Map[String, String]]]
    assertEquals(forgeFactsArr.length, 4, s"forge facts array length doesn't match")

    val respLoadedFacts = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/remote-facts/_test",
      Map(
        "Content-Type" -> s"application/json"
      ),
      Some(
        Json.obj(
          "api_url" -> fullDomain,
          "headers" -> Map(
            "Content-Type" -> s"application/json"
          ),
          "method" -> "POST"
        )
      )
    ).awaitf(5.seconds)
    assertEquals(respLoadedFacts.status, 200, s"verifier route did not respond with 200")

    assert(respLoadedFacts.json.at("done").isDefined, s"'done' field should be defined")
    assert(respLoadedFacts.json.at("done").asBoolean, s"request 'done' status should be true")
    assert(respLoadedFacts.json.at("loadedFacts").isDefined, "'loadedFacts' should be defined")
    assert(respLoadedFacts.json.at("loadedFacts.facts").isDefined, "'facts' array should be defined")

    val loadedFacts = respLoadedFacts.json.at("loadedFacts.facts").as[List[String]]
    assertEquals(loadedFacts.length, 4, s"loaded facts array length doesn't match")
  }

  test("should be able to attenuate a token with a forge_ref, keypair_ref and a body config") {
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
        facts = Seq(
          "name(\"otoroshi-biscuit-studio-test\")",
          "user(\"biscuit-test-user\")",
          "role(\"user\")"
        )
      )
    )

    val attenuatorConfig = AttenuatorConfig(
        checks = Seq(
          "check if name(\"otoroshi-biscuit-studio-test\")",
          "check if user(\"biscuit-test-user\")",
          "check if role(\"user\")",
          "check if operation(\"read\")"
        )
      )

    /// Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                       test attenuator with keypair reference                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val goodAttenuator = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/attenuators/_test", Map(
      "Content-Type" -> s"application/json"
    ), Some(Json.obj(
      "keypair_ref" -> keypair.id,
      "forge_ref" -> forge.id,
      "checks" -> attenuatorConfig.checks
    ))).awaitf(5.seconds)

    val attenuatedToken = goodAttenuator.json.at("token").asString

    assert(goodAttenuator.json.at("done").isDefined, "'done' should be defined in body response")
    assert(goodAttenuator.json.at("done").asBoolean, "'done' should be true")
    assert(goodAttenuator.json.at("token").isDefined, "token should be defined")
    assert(goodAttenuator.json.at("pubKey").isDefined, "token should be defined")

    val encodedAttenuatedToken = Biscuit.from_b64url(attenuatedToken, keypair.getPubKey)
    assertEquals(encodedAttenuatedToken.authorizer().checks().asScala.flatMap(_._2.asScala).size, forge.config.checks.size + attenuatorConfig.checks.size, s"attenuated token doesn't contain all the checks")
  }

  test("should be able to attenuate a token with a forge_ref, public and private keys") {
    val biscuitKeyPair = new KeyPair()

    val privKey = biscuitKeyPair.toHex
    val pubKey = biscuitKeyPair.public_key()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = privKey,
      pubKey = pubKey.toHex
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
        facts = Seq(
          "name(\"otoroshi-biscuit-studio-test\")",
          "user(\"biscuit-test-user\")",
          "role(\"user\")"
        )
      )
    )

    val attenuatorConfig = AttenuatorConfig(
      checks = Seq(
        "check if name(\"otoroshi-biscuit-studio-test\")",
        "check if user(\"biscuit-test-user\")",
        "check if role(\"user\")",
        "check if operation(\"read\")"
      )
    )

    /// Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                       test attenuator with keypair reference                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val goodAttenuator = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/attenuators/_test", Map(
      "Content-Type" -> s"application/json"
    ), Some(Json.obj(
      "pubKey" -> pubKey.toHex,
      "privKey" -> privKey,
      "forge_ref" -> forge.id,
      "checks" -> attenuatorConfig.checks
    ))).awaitf(5.seconds)

    val attenuatedToken = goodAttenuator.json.at("token").asString

    assert(goodAttenuator.json.at("done").isDefined, "'done' should be defined in body response")
    assert(goodAttenuator.json.at("done").asBoolean, "'done' should be true")
    assert(goodAttenuator.json.at("token").isDefined, "token should be defined")
    assert(goodAttenuator.json.at("pubKey").isDefined, "token should be defined")

    val encodedAttenuatedToken = Biscuit.from_b64url(attenuatedToken, pubKey)
    assertEquals(encodedAttenuatedToken.authorizer().checks().asScala.flatMap(_._2.asScala).size, forge.config.checks.size + attenuatorConfig.checks.size, s"attenuated token doesn't contain all the checks")
  }

  test("should be able to attenuate a token with a token input, public and private keys") {
    val biscuitKeyPair = new KeyPair()

    val privKey = biscuitKeyPair.toHex
    val pubKey = biscuitKeyPair.public_key()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = privKey,
      pubKey = pubKey.toHex
    )

    val tokenConfig = BiscuitForgeConfig(
      facts = Seq(
        "name(\"otoroshi-biscuit-studio-test\")",
        "user(\"biscuit-test-user\")",
        "role(\"user\")"
      )
    )

    val attenuatorConfig = AttenuatorConfig(
      checks = Seq(
        "check if name(\"otoroshi-biscuit-studio-test\")",
        "check if user(\"biscuit-test-user\")",
        "check if role(\"user\")",
        "check if operation(\"read\")"
      )
    )

    /// Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                       generate token                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respForgeToken = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/_generate",
      Map(
        "Content-Type" -> s"application/json"
      ),
      Some(
        Json.obj(
          "keypair_ref" -> keypair.id,
          "config" -> tokenConfig.json
        )
      )
    ).awaitf(5.seconds)
    assertEquals(respForgeToken.status, 200, s"verifier route did not respond with 200")
    assert(respForgeToken.json.at("done").isDefined, s"'done' field should be defined")
    assert(respForgeToken.json.at("done").asBoolean, s"request 'done' status should be true")
    assert(respForgeToken.json.at("pubKey").isDefined, "'pubKey' should be defined")
    assertEquals(respForgeToken.json.at("pubKey").asString, biscuitKeyPair.public_key().toHex, "public key response should match to given public key")
    assert(respForgeToken.json.at("token").isDefined, "'token' should be defined")

    assert(respForgeToken.json.at("token").isDefined, "token should be successfully generated")

    val genToken = respForgeToken.json.at("token").asString
    val encodedBiscuit = Biscuit.from_b64url(genToken, biscuitKeyPair.public_key())

    assertEquals(encodedBiscuit.authorizer().facts().size(), tokenConfig.facts.length, s"generated token doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, tokenConfig.checks.length, s"generated token doesn't contain all checks")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                       test attenuator with keypair reference                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val goodAttenuator = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/attenuators/_test", Map(
      "Content-Type" -> s"application/json"
    ), Some(Json.obj(
      "pubKey" -> pubKey.toHex,
      "privKey" -> privKey,
      "token" -> genToken,
      "checks" -> attenuatorConfig.checks
    ))).awaitf(5.seconds)

    val attenuatedToken = goodAttenuator.json.at("token").asString
    
    assert(goodAttenuator.json.at("done").isDefined, "'done' should be defined in body response")
    assert(goodAttenuator.json.at("done").asBoolean, "'done' should be true")
    assert(goodAttenuator.json.at("token").isDefined, "token should be defined")
    assert(goodAttenuator.json.at("pubKey").isDefined, "token should be defined")

    val encodedAttenuatedToken = Biscuit.from_b64url(attenuatedToken, pubKey)
    assertEquals(encodedAttenuatedToken.authorizer().checks().asScala.flatMap(_._2.asScala).size, tokenConfig.checks.size + attenuatorConfig.checks.size, s"attenuated token doesn't contain all the checks")
  }
}
