package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitForgeConfig, BiscuitKeyPair}
import org.biscuitsec.biscuit.crypto.KeyPair
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.utils.syntax.implicits._
import play.api.libs.json.Json

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

}
