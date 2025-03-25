package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import org.biscuitsec.biscuit.crypto.{KeyPair, PublicKey}
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.models.EntityLocation
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import play.api.libs.json.Json
import reactor.core.publisher.Mono

import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._

class TestsTokensForge extends BiscuitStudioOneOtoroshiServerPerSuite {
  test(s"create token from forge entity") {
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
      keypairRef = keypair.id,
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


    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    await(5.seconds)

    val forgreCreationResp = client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge).awaitf(3.seconds)

    assert(forgreCreationResp.created, s"forge entity has not been created")
    assert(forgreCreationResp.bodyJson.at("config.facts").isDefined, s"configuration facts are missing")
    assert(forgreCreationResp.bodyJson.at("config.checks").isDefined, s"configuration checks are missing")
    assert(forgreCreationResp.bodyJson.at("config.facts").as[List[String]].nonEmpty, "list of facts should not be empty")
    assert(forgreCreationResp.bodyJson.at("config.checks").as[List[String]].nonEmpty, "list of checks should not be empty")
    assertEquals(forgreCreationResp.bodyJson.at("config.facts").as[List[String]].size, 3, "list of checks should not be empty")
    assertEquals(forgreCreationResp.bodyJson.at("config.checks").as[List[String]].size, 2, "list of checks should not be empty")

  }

  test(s"create a token forge with remote facts loader entity") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  create API roles route                                        ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val (tport, _) = createTestServerWithRoutes("test", routes => routes.post("/api/roles", (req, response) => {
      req.receive().retain().asString().flatMap { body =>
        response
          .status(200)
          .addHeader("Content-Type", "application/json")
          .sendString(Mono.just(
            s"""{
               |"acl": [
               |{
               |"user": "1234",
               |"resource": "resource1",
               |"action": "read"
               |},
               |{
               |"user": "1234",
               |"resource": "resource1",
               |"action": "write"
               |},
               |{
               |"user": "1234",
               |"resource": "resource2",
               |"action": "read"
               |}
               |]
               |}""".stripMargin))
      }
    }))

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  create remote facts loader entity                             ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val biscuitKeyPair = new KeyPair()
    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex
    )

    val rfl = RemoteFactsLoader(
      id = IdGenerator.namedId("biscuit-remote-facts", otoroshi.env),
      name = "New biscuit remote facts loader",
      description = "New biscuit remote facts loader",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitRemoteFactsConfig(
        apiUrl = s"http://test.oto.tools:${tport}/api/roles",
        headers = Map(
          "Content-Type" -> "application/json"
        )
      )
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
      remoteFactsLoaderRef = rfl.id.some
    )

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-remote-facts").upsertEntity(rfl)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(2500.millis)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test API Roles route                                          ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val resp = client.call("POST", s"http://test.oto.tools:${tport}/api/roles", Map("Content-Type" -> "application/json"), Some(Json.obj("foo" -> "bar"))).awaitf(5.seconds)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    assert(resp.json.at("acl").isDefined, s"acl array is not defined")

    val aclArr = resp.json.at("acl").as[List[Map[String, String]]]
    assertEquals(aclArr.length, 3, s"acl array length doesn't match")

    await(2500.millis)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test biscuit creation from forge                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val resp2 = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/_generate", Map("Content-Type" -> s"application/json"), Some(Json.obj(
      "config" -> forge.config.json,
      "keypair_ref" -> keypair.id,
      "remoteFactsLoaderRef" -> forge.remoteFactsLoaderRef.get,
    ))).awaitf(5.seconds)
    assertEquals(resp2.status, 200, s"verifier route did not respond with 200")
    assert(resp2.json.at("done").isDefined, s"acl array is not defined")
    assert(resp2.json.at("done").asBoolean, s"acl array is not defined")
    assert(resp2.json.at("token").isDefined, s"acl array is not defined")

    val token = BiscuitExtractorConfig.replaceHeader(resp2.json.at("token").get.asString)
    assert(token.nonEmpty, s"token is empty")

    val publicKeyFormatted = new PublicKey(keypair.getCurrentAlgo, keypair.pubKey)

    val encodedBiscuit = Biscuit.from_b64url(token, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().facts().size(), aclArr.length, s"token doesn't contain all remote facts")

    await(2500.millis)
  }

  test(s"create a token with forge from API") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup forge                                                   ///////////
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

    val forge = BiscuitTokenForge(
      id = IdGenerator.namedId("biscuit-forge", otoroshi.env),
      name = "New biscuit token",
      description = "New biscuit token",
      keypairRef = keypair.id,
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitForgeConfig(
        checks = Seq(
          "check if user(\"biscuit-demo\")"
        ),
        facts = Seq(
          "user(\"biscuit-demo\");",
          "role(\"1234\");",
        ),
        resources = List.empty,
        rules = List.empty
      ),
      remoteFactsLoaderRef = None
    )

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(2500.millis)


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test API                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val resp = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${forge.id}/_generate",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), None).awaitf(5.seconds)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    assert(resp.json.at("token").isDefined, s"token not generated")

    val token = BiscuitExtractorConfig.replaceHeader(resp.json.at("token").get.asString)
    assert(token.nonEmpty, s"token is empty")

    val publicKeyFormatted = new PublicKey(keypair.getCurrentAlgo, keypair.pubKey)

    val encodedBiscuit = Biscuit.from_b64url(token, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().facts().size(), forge.config.facts.length, s"token doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, forge.config.checks.length, s"token doesn't contain all checks")

    await(2500.millis)
  }

}