package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import org.biscuitsec.biscuit.crypto.KeyPair
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.models.EntityLocation
import otoroshi.next.models.{NgBackend, NgDomainAndPath, NgFrontend, NgPluginInstance, NgPluginInstanceConfig, NgPlugins, NgRoute, NgTarget}
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.{BiscuitExposePubKeysPluginConfig, BiscuitTokenAttenuatorPlugin, BiscuitTokenVerifierPlugin, ExposeBiscuitPublicKeysPlugin}
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._

class PluginsChainSuite extends BiscuitStudioOneOtoroshiServerPerSuite {
  test("should be able to chain a verifier and attenuator plugins") {
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

    val badForge = BiscuitTokenForge(
      id = IdGenerator.namedId("biscuit-forge", otoroshi.env),
      name = "New biscuit token",
      description = "New biscuit token",
      keypairRef = keypair.id,
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitForgeConfig(
        facts = Seq(
          "name(\"otoroshi-biscuit-studio-test2\")",
          "user(\"biscuit-test-user2\")",
          "role(\"user2\")"
        ),
        checks = Seq(
          "check if server(\"biscuit-server-test\")"
        )
      )
    )

    val goodForge = BiscuitTokenForge(
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
          "check if server(\"biscuit-test-server\")"
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
          "server(\"biscuit-test-server\")",
          "operation(\"read\")"
        )
      ),
      extractor = BiscuitExtractorConfig(
        extractorType = "query",
        extractorName = "biscuit-token"
      )
    )
    val attenuator = BiscuitAttenuator(
      id = IdGenerator.namedId("biscuit-attenuator", otoroshi.env),
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = AttenuatorConfig(
        checks = Seq(
          "check if operation(\"read\")",
          "check if email(\"demo@biscuit-studio.com\")"
        )
      )
    )

    val frontendDomain = "test-plugins-chain.oto.tools"

    val routeEndpoint = NgRoute(
      location = EntityLocation.default,
      id = UUID.randomUUID().toString,
      name = "test plugins chain route",
      description = "test plugins chain route",
      tags = Seq.empty,
      metadata = Map.empty,
      enabled = true,
      debugFlow = true,
      capture = false,
      exportReporting = false,
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath(frontendDomain))),
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
        ),
        NgPluginInstance(
          plugin = s"cp:${classOf[BiscuitTokenAttenuatorPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "attenuator_ref" -> attenuator.id,
            "extractor_type" -> "query",
            "extractor_name" -> "biscuit-token",
            "token_replace_loc" -> "cookie",
            "token_replace_name" -> "biscuit-attenuated-token"
          ))
        )
      ))
    )

    /// Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(badForge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(goodForge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(verifier)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuators").upsertEntity(attenuator)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeEndpoint)

    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test biscuit creation from BAD forge                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val tokenRespBadForge = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${badForge.id}/_generate", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), Some(Json.obj())).awaitf(5.seconds)

    assert(tokenRespBadForge.json.at("token").isDefined, "token should be successfully generated")

    val genBadToken = tokenRespBadForge.json.at("token").asString
    val encodedBadToken = Biscuit.from_b64url(genBadToken, publicKeyFormatted)

    assertEquals(encodedBadToken.authorizer().facts().size(), goodForge.config.facts.length, s"token doesn't contain all facts")
    assertEquals(encodedBadToken.authorizer().checks().asScala.flatMap(_._2.asScala).size, goodForge.config.checks.length, s"token doesn't contain all checks")


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test biscuit creation from GOOD forge                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val tokenResp = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${goodForge.id}/_generate", Map(
      "Content-Type" -> s"application/json",
      "Otoroshi-Client-Id" -> "admin-api-apikey-id",
      "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
    ), Some(Json.obj())).awaitf(5.seconds)

    assert(tokenResp.json.at("token").isDefined, "token should be successfully generated")

    val genGoodToken = tokenResp.json.at("token").asString
    val encodedGoodToken = Biscuit.from_b64url(genGoodToken, publicKeyFormatted)

    assertEquals(encodedGoodToken.authorizer().facts().size(), goodForge.config.facts.length, s"token doesn't contain all facts")
    assertEquals(encodedGoodToken.authorizer().checks().asScala.flatMap(_._2.asScala).size, goodForge.config.checks.length, s"token doesn't contain all checks")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                       call the route WITH BAD TOKEN - token should NOT be verified and attenuated             ///////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respAttenuatorRouteCallBadToken = client.call("GET", s"http://${frontendDomain}:${port}/?biscuit-token=${genBadToken}", Map.empty, None).awaitf(5.seconds)
    assertEquals(respAttenuatorRouteCallBadToken.status, 403, s"bad generated token - should throw forbidden error")

    assert(respAttenuatorRouteCallBadToken.json.at("Otoroshi-Error").isDefined, s"'Otoroshi-Error' should be defined")
    assert(respAttenuatorRouteCallBadToken.json.at("Otoroshi-Error").asString.contains("Biscuit token is not valid"), s"'Otoroshi-Error' should contains 'Biscuit token is not valid' error")

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                       call the route WITH GOOD TOKEN - token should be verified and attenuated          ///////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respAttenuatorRouteCallGoodToken = client.call("GET", s"http://${frontendDomain}:${port}/?biscuit-token=${genGoodToken}", Map.empty, None).awaitf(5.seconds)

    assertEquals(respAttenuatorRouteCallGoodToken.status, 200, s"attenuator route did not respond with 200")

    assert(respAttenuatorRouteCallGoodToken.json.at("cookies.biscuit-attenuated-token.value").isDefined, s"response cookies don't contains the biscuit attenuated token")
    assert(respAttenuatorRouteCallGoodToken.json.at("cookies.biscuit-attenuated-token.value").asString.nonEmpty, s"response cookies don't contains the biscuit attenuated token")

    val attenuatedGoodToken = BiscuitExtractorConfig.replaceHeader(respAttenuatorRouteCallGoodToken.json.at("cookies.biscuit-attenuated-token.value").asString)
    assert(attenuatedGoodToken.nonEmpty, s"attenuated token is empty")

    val encodedAttenuatedGoodBiscuit = Biscuit.from_b64url(attenuatedGoodToken, keypair.getPubKey)
    assertEquals(encodedAttenuatedGoodBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, goodForge.config.checks.size + attenuator.config.checks.size, s"attenuated token doesn't contain checks list")

  }
}