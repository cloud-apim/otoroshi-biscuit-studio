package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import org.biscuitsec.biscuit.crypto.KeyPair
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.models.EntityLocation
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import play.api.libs.json.Json

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.DurationInt

class AdminAPISuite extends BiscuitStudioOneOtoroshiServerPerSuite {
  test("should be able to generate a token from the ADMIN API with a Forge entity") {
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
          "check if user(\"biscuit-test-user\")",
          "check if role(\"user\")"
        )
      )
    )

    /// Add entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test biscuit creation from forge                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val tokenResp = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${forge.id}/_generate", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), Some(Json.obj())).awaitf(5.seconds)

    assert(tokenResp.json.at("token").isDefined, "token should be successfully generated")

    val genToken = tokenResp.json.at("token").asString
    val encodedBiscuit = Biscuit.from_b64url(genToken, publicKeyFormatted)

    assertEquals(encodedBiscuit.authorizer().facts().size(), forge.config.facts.length, s"generated token from forge ADMIN API doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, forge.config.checks.length, s"generated token from forge ADMIN API doesn't contain all checks")
  }

  test("should be able to generate a token from the ADMIN API from body parameters with keypair Ref") {
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

    val config = BiscuitForgeConfig(
      facts = Seq(
        "user(\"demo\")",
        "role(\"test\")",
      ),
      checks = Seq(
        "check if user(\"demo\")",
        "check if role(\"test\")"
      )
    )

    /// Add entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                          test biscuit creation from forge with body parameters                 ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val tokenResp = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/tokens/_generate", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), Some(Json.obj(
      "keypair_ref" -> keypair.id,
      "config" -> config.json
    ))).awaitf(5.seconds)

    assert(tokenResp.json.at("token").isDefined, "token should be successfully generated")


    val genToken = tokenResp.json.at("token").asString
    val encodedBiscuit = Biscuit.from_b64url(genToken, publicKeyFormatted)

    assertEquals(encodedBiscuit.authorizer().facts().size(), config.facts.length, s"genereated token from body parameters WITH keypair ref doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, config.checks.length, s"genereated token from body parameters WITH keypair ref doesn't contain all checks")
  }

  test("should be able to generate a token from the ADMIN API from body parameters without keypair Ref") {
    val biscuitKeyPair = new KeyPair()
    val privKey = biscuitKeyPair.toHex
    val pubKey = biscuitKeyPair.public_key()

    val config = BiscuitForgeConfig(
      facts = Seq(
        "user(\"demo\")",
        "role(\"test\")",
      ),
      checks = Seq(
        "check if user(\"demo\")",
        "check if role(\"test\")"
      )
    )

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                          test biscuit creation from forge with body parameters                 ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val tokenResp = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/tokens/_generate", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), Some(Json.obj(
      "privKey"-> privKey,
      "pubKey"-> pubKey.toHex,
      "config" -> config.json
    ))).awaitf(5.seconds)

    assert(tokenResp.json.at("token").isDefined, "token should be successfully generated")

    val genToken = tokenResp.json.at("token").asString
    val encodedBiscuit = Biscuit.from_b64url(genToken, pubKey)

    assertEquals(encodedBiscuit.authorizer().facts().size(), config.facts.length, s"genereated token from body parameters WITHOUT keypair ref doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, config.checks.length, s"genereated token from body parameters WITHOUT keypair ref doesn't contain all checks")
  }

  test("should be able to generate a token from the ADMIN API with a Forge entity and then verify it with a verifier entity") {
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

    val biscuitKeyPair2 = new KeyPair()
    val keypair2 = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = biscuitKeyPair2.toHex,
      pubKey = biscuitKeyPair2.public_key().toHex
    )

    val publicKeyFormatted = keypair.getPubKey

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

    val verifier = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = VerifierConfig(
        checks = Seq(
          "check if name(\"otoroshi-biscuit-studio-test\")",
          "check if user(\"biscuit-test-user\")",
          "check if role(\"user\")"
        ),
        facts = Seq(
          "server(\"biscuit-server-test\")",
          "operation(\"read\")"
        )
      ),
      extractor = BiscuitExtractorConfig()
    )

    val badVerifierKeypair = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair2.id,
      config = VerifierConfig(),
      extractor = BiscuitExtractorConfig()
    )

    val badVerifierChecks = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = VerifierConfig(
        checks = Seq(
          "check if name(\"otoroshi-biscuit-studio-test\")",
          "check if user(\"biscuit-test-user\")",
          "check if role(\"admin\")"
        ),
        facts = Seq(
          "operation(\"read\")"
        )
      ),
      extractor = BiscuitExtractorConfig()
    )

    /// Add entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair2)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(verifier)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(badVerifierChecks)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(badVerifierKeypair)
    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test biscuit creation from forge                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val tokenResp = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${forge.id}/_generate", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), Some(Json.obj())).awaitf(5.seconds)

    assert(tokenResp.json.at("token").isDefined, "token should be successfully generated")

    val genToken = tokenResp.json.at("token").asString
    val encodedBiscuit = Biscuit.from_b64url(genToken, publicKeyFormatted)

    assertEquals(encodedBiscuit.authorizer().facts().size(), forge.config.facts.length, s"token doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, forge.config.checks.length, s"token doesn't contain all checks")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test BAD biscuit verifier keypair                             ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respBadVerifierKeypairRef = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-verifiers/${badVerifierKeypair.id}/_verify", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), Some(Json.obj(
      "token" -> genToken
    ))).awaitf(5.seconds)

    assert(respBadVerifierKeypairRef.json.at("error").isDefined, "error for bad verifier with bad checks should be defined")
    assertEquals(respBadVerifierKeypairRef.json.at("error").asString, "Err(Format(Signature(InvalidFormat(\"signature error: Verification equation was not satisfied\"))))", "error for bad verifier with bad checks should be defined")


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test BAD biscuit verifier checks                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respBadVerifierChecks = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-verifiers/${badVerifierChecks.id}/_verify", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), Some(Json.obj(
      "token" -> genToken
    ))).awaitf(5.seconds)

    assert(respBadVerifierChecks.json.at("error").isDefined, "error for bad verifier with bad checks should be defined")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test Good biscuit verifier                                    ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val goodVerifier = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-verifiers/${verifier.id}/_verify", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), Some(Json.obj(
      "token" -> genToken
    ))).awaitf(5.seconds)

    assert(goodVerifier.json.at("status").isDefined, "status for good verifier should be defined")
    assert(goodVerifier.json.at("message").isDefined, "message for good verifier should be defined")
    assertEquals(goodVerifier.json.at("status").asString, "success", "status message should be success")
    assertEquals(goodVerifier.json.at("message").asString, "Checked successfully", "message should be 'Checked successfully'")
  }

  test("should be able to verify a token from the ADMIN API with a FORGE ref and body parameters") {
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
    val goodVerifier = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/tokens/_verify", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), Some(Json.obj(
      "forge_ref" -> forge.id,
      "keypair_ref"-> keypair.id,
      "config" -> verifierConfig.json
    ))).awaitf(5.seconds)

    assert(goodVerifier.json.at("status").isDefined, "status for good verifier should be defined")
    assert(goodVerifier.json.at("message").isDefined, "message for good verifier should be defined")
    assertEquals(goodVerifier.json.at("status").asString, "success", "status message should be success")
    assertEquals(goodVerifier.json.at("message").asString, "Checked successfully", "message should be 'Checked successfully'")
  }
}