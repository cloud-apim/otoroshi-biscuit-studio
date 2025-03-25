package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitExtractorConfig, BiscuitKeyPair, BiscuitVerifier, VerifierConfig}
import org.biscuitsec.biscuit.crypto.KeyPair
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.models.EntityLocation
import otoroshi.next.models._
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenVerifierPlugin
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.duration.DurationInt

class VerifiersSuite extends BiscuitStudioOneOtoroshiServerPerSuite {

  test(s"should be able to create a verifier entity") {
    val verifier = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
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

    val verifierRes = client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(verifier).awaitf(5.seconds)

    assert(verifierRes.created, s"verifier has not been created")
    assert(verifierRes.bodyJson.at("config.facts").isDefined, s"facts are not defined")
    assert(verifierRes.bodyJson.at("config.checks").isDefined, s"checks are not defined")
    assertEquals(verifierRes.bodyJson.at("config.facts").as[List[String]].size, verifier.config.facts.size, s"list of facts doesn't contain all the config")
    assertEquals(verifierRes.bodyJson.at("config.checks").as[List[String]].size, verifier.config.checks.size, s"list of checks doesn't contain all the config")
  }

  test(s"testing good verifier plugin policies") {
    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
      isPublic = true
    )

    val encodedToken = Biscuit.builder(biscuitKeyPair)
      .add_authority_fact("user(\"biscuit-test-user\")")
      .add_authority_fact("role(\"user\")")
      .build().serialize_b64url()

    val verifier = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = VerifierConfig(
        checks = Seq(
          "check if user(\"biscuit-test-user\")",
          "check if role(\"user\")"
        ),
        facts = Seq(
          "server(\"biscuit-server-test\")",
          "operation(\"read\")"
        )
      ),
      extractor = BiscuitExtractorConfig(
        extractorName = "biscuit-header"
      )
    )

    val routeDomain = s"verifier.oto.tools"
    val routeWithVerifier = NgRoute(
      location = EntityLocation.default,
      id = UUID.randomUUID().toString,
      name = "test verifier route",
      description = "test verifier route",
      tags = Seq.empty,
      metadata = Map.empty,
      enabled = true,
      debugFlow = false,
      capture = false,
      exportReporting = false,
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath(routeDomain))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(
        NgPluginInstance(
          plugin = s"cp:otoroshi.next.plugins.EchoBackend",
          config = NgPluginInstanceConfig(Json.obj(
            "limit" -> "524288"
          ))
        ),
        NgPluginInstance(
          plugin = s"cp:${classOf[BiscuitTokenVerifierPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "verifier_refs" -> Seq(
              verifier.id
            ),
            "enforce" -> true
          ))
        )
      ))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(verifier)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeWithVerifier)

    await(3.seconds)

    val headers = Map(
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(5.seconds)
    println(s"resp.body = ${resp.json}")
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")

    // Teardown
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeWithVerifier)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").deleteEntity(verifier)

    await(5.seconds)
  }

  test(s"testing verifier plugin with bad token") {
    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
      isPublic = true
    )

    val badToken = "EcYBCiwKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIIggKBggGEgIYChIkCAASIIjCXLU5A-JBhCzBWklpKOr4azUhMLMXQzrhcLXTqxsfGkA6aVZxS-uu8tpQtbK_NxeaGxpc-WPBYPgO83NZQZSdLbE4ELcfgn-6OoH-jp6Ych7M_T3t1vBoNnSp4Paah9UHIiIKIBvwbLEZSZUtv2sQCY_UUBI-wBjBk9gnHXW4uQRGHzSv"

    val verifier = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = VerifierConfig(
        checks = Seq("check if user(\"biscuit-test-user\")"),
        policies = Seq("allow if role(\"admin\")"),
      ),
      extractor = BiscuitExtractorConfig(
        extractorName = "biscuit-header"
      )
    )

    val routeDomain = s"verifier.oto.tools"
    val routeWithVerifier = NgRoute(
      location = EntityLocation.default,
      id = UUID.randomUUID().toString,
      name = "test verifier route",
      description = "test verifier route",
      tags = Seq.empty,
      metadata = Map.empty,
      enabled = true,
      debugFlow = false,
      capture = false,
      exportReporting = false,
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath(routeDomain))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(
        NgPluginInstance(
          plugin = s"cp:otoroshi.next.plugins.EchoBackend",
          config = NgPluginInstanceConfig(Json.obj(
            "limit" -> "524288"
          ))
        ),
        NgPluginInstance(
          plugin = s"cp:${classOf[BiscuitTokenVerifierPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "verifier_refs" -> Seq(
              verifier.id
            ),
            "enforce" -> true
          ))
        )
      ))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(verifier)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeWithVerifier)
    await(3.seconds)

    val headers = Map(
      "biscuit-header" -> badToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(5.seconds)

    println(s"resppp : ${resp.body}")
    assertEquals(resp.status, 403, s"verifier should thrown a forbidden error")

    // Teardown
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeWithVerifier)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(keypair)
    await(3.seconds)
  }

  test(s"testing verifier plugin with bad policies") {
    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
      isPublic = true
    )

    val encodedToken = Biscuit.builder(biscuitKeyPair)
      .add_authority_fact("user(\"biscuit-test-user\")")
      .add_authority_fact("role(\"user\")")
      .build().serialize_b64url()

    val verifier = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = VerifierConfig(
        checks = Seq("check if user(\"biscuit-test-user\")"),
        policies = Seq("allow if role(\"admin\")"),
      ),
      extractor = BiscuitExtractorConfig(
        extractorName = "biscuit-header"
      )
    )

    val routeDomain = s"verifier.oto.tools"
    val routeWithVerifier = NgRoute(
      location = EntityLocation.default,
      id = UUID.randomUUID().toString,
      name = "test verifier route",
      description = "test verifier route",
      tags = Seq.empty,
      metadata = Map.empty,
      enabled = true,
      debugFlow = false,
      capture = false,
      exportReporting = false,
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath(routeDomain))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(
        NgPluginInstance(
          plugin = s"cp:otoroshi.next.plugins.EchoBackend",
          config = NgPluginInstanceConfig(Json.obj(
            "limit" -> "524288"
          ))
        ),
        NgPluginInstance(
          plugin = s"cp:${classOf[BiscuitTokenVerifierPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "verifier_refs" -> Seq(
              verifier.id
            ),
            "enforce" -> true
          ))
        )
      ))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(verifier)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeWithVerifier)
    await(3.seconds)

    val headers = Map(
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(5.seconds)

    println(s"resppp : ${resp.body}")
    assertEquals(resp.status, 403, s"verifier should thrown a forbidden error")

    // Teardown
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeWithVerifier)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(keypair)
    await(3.seconds)
  }

  test(s"testing role admin") {
    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
      isPublic = true
    )

    val encodedToken = Biscuit.builder(biscuitKeyPair)
      .add_authority_fact("role(\"admin\")")
      .build().serialize_b64url()

    val conf = VerifierConfig(
      checks = List.empty,
      facts = List.empty,
      resources = List.empty,
      rules = List.empty,
      policies = Seq(
        "allow if role(\"admin\");"
      ),
      revokedIds = List.empty
    )

    val verifier = BiscuitVerifier(
      id =  IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = conf,
      extractor = BiscuitExtractorConfig(
        extractorName = "biscuit-header"
      )
    )

    val routeDomain = s"verifier.oto.tools"
    val routeWithVerifier = NgRoute(
      location = EntityLocation.default,
      id = UUID.randomUUID().toString,
      name = "test verifier route",
      description = "test verifier route",
      tags = Seq.empty,
      metadata = Map.empty,
      enabled = true,
      debugFlow = false,
      capture = false,
      exportReporting = false,
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath(routeDomain))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(
        NgPluginInstance(
          plugin = s"cp:otoroshi.next.plugins.EchoBackend",
          config = NgPluginInstanceConfig(Json.obj(
            "limit" -> "524288"
          ))
        ),
        NgPluginInstance(
          plugin = s"cp:${classOf[BiscuitTokenVerifierPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "verifier_refs" -> Seq(
              verifier.id
            ),
            "enforce" -> true
          ))
        )
      ))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(verifier)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeWithVerifier)
    await(3.seconds)

    val headers = Map(
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(5.seconds)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")

    // Teardown
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeWithVerifier)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(keypair)

    await(3.seconds)
  }

  test(s"testing token revoked") {

    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
      isPublic = true
    )

    val biscuitToken = Biscuit.builder(biscuitKeyPair)
      .add_authority_fact("user(\"test\")")
      .build()

    val encodedToken = biscuitToken.serialize_b64url()

    assert(!biscuitToken.revocation_identifiers().isEmpty, "revocation ids array should not be empty")
    assertEquals(biscuitToken.revocation_identifiers().size, 1, "size of revocation ids should be 1")
    assert(biscuitToken.revocation_identifiers().get(0).toHex.nonEmpty, "srevocation token should not be empty")

    val demoKeyPair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      pubKey = "5eb7e77fed63b366bd9f0aad29f34c54b213975d9b500d16781190d36fbef64b",
      privKey = "284702b00861cdaaa5bc868768a06dbec3ebffe863db2c3d12aa413427a68493"
    )

    val verifier = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", otoroshi.env),
      keypairRef = demoKeyPair.id,
      config = VerifierConfig(
        policies = Seq(
          "allow if user(\"test\");"
        ),
        revokedIds = Seq(
          biscuitToken.revocation_identifiers().get(0).toHex
        )
      ),
      extractor = BiscuitExtractorConfig(
        extractorName = "biscuit-header"
      )
    )

    val routeDomain = s"verifier.oto.tools"
    val routeWithVerifier = NgRoute(
      location = EntityLocation.default,
      id = UUID.randomUUID().toString,
      name = "test verifier route",
      description = "test verifier route",
      tags = Seq.empty,
      metadata = Map.empty,
      enabled = true,
      debugFlow = false,
      capture = false,
      exportReporting = false,
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath(routeDomain))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(
        NgPluginInstance(
          plugin = s"cp:otoroshi.next.plugins.EchoBackend",
          config = NgPluginInstanceConfig(Json.obj(
            "limit" -> "524288"
          ))
        ),
        NgPluginInstance(
          plugin = s"cp:${classOf[BiscuitTokenVerifierPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "verifier_refs" -> Seq(
                verifier.id
            ),
            "enforce" -> true
          ))
        )
      ))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(demoKeyPair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(verifier)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeWithVerifier)
    await(3.seconds)

    val headers = Map(
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(5.seconds)
    assertEquals(resp.status, 403, s"verifier route did not respond with 403")

    // Teardown
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeWithVerifier)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(demoKeyPair)

    await(3.seconds)
  }

  test(s"testing unauthorized exception thrown by biscuit verifier") {
    val token = "EtABCmYKBWZpbGUxCgVmaWxlMgoBMBgDIg0KCwgEEgMYgAgSAhgAIg0KCwgEEgMYgQgSAhgAIg0KCwgEEgMYgAgSAhgBMiQKIgoCCBsSBwgCEgMIgggSBggDEgIYABILCAQSAwiCCBICGAASJAgAEiAReFc-Dulaeduh6rLHBknsrLzIpfxO1n-RlWZ3uhsRFRpAQ-mWK41KHy9xWuZzfFzU18yzEMd3uzEy3pbnhsxFAPc1zIDNrN4jHSKLZLy6fxYs3ZpjFyGjCbbfpBAklzRoAyIiCiBEX-xJldZRt4CugAJnJZqm54Zpm_5_crHLHNITeAOijw=="

    val keypairID = s"biscuit-keypair_${UUID.randomUUID().toString}"
    val verifierId = s"biscuit-verifier_${UUID.randomUUID().toString}"
    val routeVerifierId = s"${UUID.randomUUID().toString}"
    val routeDomain = s"${UUID.randomUUID().toString}.oto.tools"

    val demoKeyPair = BiscuitKeyPair(
      id = keypairID,
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      pubKey = "1055c750b1a1505937af1537c626ba3263995c33a64758aaafb1275b0312e284",
      privKey = "99e87b0e9158531eeeb503ff15266e2b23c2a2507b138c9d1b1f2ab458df2d61"
    )

    val verifier = BiscuitVerifier(
      id = verifierId,
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = demoKeyPair.id,
      config = VerifierConfig(
        checks = List.empty,
        facts = Seq(
          "resource(\"file1\");"
        ),
        resources = List.empty,
        rules = List.empty,
        policies = Seq(
          "allow if true;"
        ),
        revokedIds = Seq(
          "7595a112a1eb5b81a6e398852e6118b7f5b8cbbff452778e655100e5fb4faa8d3a2af52fe2c4f9524879605675fae26adbc4783e0cafc43522fa82385f396c03",
          "45f4c14f9d9e8fa044d68be7a2ec8cddb835f575c7b913ec59bd636c70acae9a90db9064ba0b3084290ed0c422bbb7170092a884f5e0202b31e9235bbcc1650d"
        )
      ),
      extractor = BiscuitExtractorConfig()
    )

    val routeWithVerifier = NgRoute(
      location = EntityLocation.default,
      id = UUID.randomUUID().toString,
      name = "test verifier route",
      description = "test verifier route",
      tags = Seq.empty,
      metadata = Map.empty,
      enabled = true,
      debugFlow = false,
      capture = false,
      exportReporting = false,
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath(routeDomain))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(
        NgPluginInstance(
          plugin = s"cp:otoroshi.next.plugins.EchoBackend",
          config = NgPluginInstanceConfig(Json.obj(
            "limit" -> "524288"
          ))
        ),
        NgPluginInstance(
          plugin = s"cp:${classOf[BiscuitTokenVerifierPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "verifier_refs" -> Seq(
              verifierId
            ),
            "enforce" -> true
          ))
        )
      ))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(demoKeyPair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(verifier)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeWithVerifier)
    await(3.seconds)

    val headers = Map(
      "Authorization" -> token
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(5.seconds)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")

    val resp2 = client.call("POST", s"http://${routeDomain}:${port}", headers, None).awaitf(5.seconds)
    assertEquals(resp2.status, 403, s"verifier route did not respond with forbidden 403")

    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(demoKeyPair)

    await(2500.millis)
  }
}
