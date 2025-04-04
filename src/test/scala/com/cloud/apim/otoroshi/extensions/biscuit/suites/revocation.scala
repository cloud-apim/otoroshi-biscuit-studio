package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiClusterPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitExtractorConfig, BiscuitKeyPair, BiscuitVerifier, VerifierConfig}
import org.joda.time.DateTime
import otoroshi.models.EntityLocation
import otoroshi.next.models.{NgBackend, NgDomainAndPath, NgFrontend, NgPluginInstance, NgPluginInstanceConfig, NgPlugins, NgRoute, NgTarget}
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits.{BetterFuture, BetterJsValue}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.{BiscuitTokenAttenuatorPlugin, BiscuitTokenVerifierPlugin}
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.duration.DurationInt

class RevocationSuite extends BiscuitStudioOneOtoroshiClusterPerSuite {

  test("should revoke on all cluster") {

    val token = "Eo0BCiMKBDEyMzQYAyIJCgcIChIDGIAIMg4KDAoCCBsSBggDEgIYABIkCAASIO5XgWOCNcmU7hLSWg5kl9CcJuOfGg049qB6Msxi43MQGkAaT-TlhUULsSNCdhJmM-Ixl-VNCwnhiP3Ijjc0RiSb6uJOeDOxl0bmKlg6kNb6bygZCAtFvnfbC7scMzvAfxEHGoIBChgKA2RldhgDMg8KDQoCCBsSBwgGEgMYgQgSJAgAEiD73BEy0MywJgKQRGileTkSqOHIoMUiMZHMtoxNpE9AaxpAMon9LKykhc0YwHi6V5hUaI9IEYdLGIr36bPoSFhtZrHqnRzQqa5jdkPScvPWvoyct1t4gop4DJ-KLDYWYCBoCCIiCiAnReFaEoEjZtLxePwTKU7KbnvIW9_v0WlJvcMp44mMTg=="
    val revocationId = "3289fd2caca485cd18c078ba579854688f4811874b188af7e9b3e848586d66b1ea9d1cd0a9ae637643d272f3d6be8c9cb75b78828a780c9f8a2c361660206808"
    val revokedTokenBody = Some(Json.arr(
      Json.obj(
        "id" -> revocationId,
        "reason" -> "fraud",
        "revocation_date" -> DateTime.now().toString()
      ))
    )

    // Call leader to revoke a token
    val resRevocation = leaderClient.call("POST", s"http://otoroshi-api.oto.tools:${leaderPort}/api/extensions/biscuit/tokens/revocation/_revoke",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), revokedTokenBody).awaitf(5.seconds)


    assertEquals(resRevocation.status, 200, "status should be 200")
    assert(resRevocation.json.at("total_revoked").isDefined, "total_revoked should be defined")
    assertEquals(resRevocation.json.at("total_revoked").as[Int], 1, "total_revoked nb should be 1")

    val prevLeader = env.adminExtensions.extension[BiscuitExtension].get.datastores.biscuitRevocationDataStore.exists(revocationId).awaitf(2.seconds)
    val revTokenWrk1Prev = envWorker.adminExtensions.extension[BiscuitExtension].get.datastores.biscuitRevocationDataStore.exists(revocationId).awaitf(2.seconds)
    val revTokenWrk2Prev = envWorker2.adminExtensions.extension[BiscuitExtension].get.datastores.biscuitRevocationDataStore.exists(revocationId).awaitf(2.seconds)

    assert(prevLeader, "leader SHOULD get the revoked token before revocation distribution")
    assert(!revTokenWrk1Prev, "worker 1 should NOT get the revoked token before revocation distribution")
    assert(!revTokenWrk2Prev, "worker 2 should NOT get the revoked token before revocation distribution")

    await(15.seconds)

    val revTokenWrk1 = envWorker.adminExtensions.extension[BiscuitExtension].get.datastores.biscuitRevocationDataStore.exists(revocationId).awaitf(2.seconds)
    val revTokenWrk2 = envWorker2.adminExtensions.extension[BiscuitExtension].get.datastores.biscuitRevocationDataStore.exists(revocationId).awaitf(2.seconds)

    assert(revTokenWrk1, "worker 1 should get the revoked token")
    assert(revTokenWrk2, "worker 2 should get the revoked token")

    // Call a verifier plugin with the revoked token
    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = "4379BE5B9AFA1A84F59D2417C20020EF1E47E0805945535B45616209D8867E50",
      pubKey = "771F9E7FE62784502FE34CE862220586D3DB637D6A5ABAD254F7330369D3B357"
    )

    val validator = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", env),
      name = "New biscuit verifier",
      description = "New biscuit verifier",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = VerifierConfig(
        checks = Seq.empty,
        facts = Seq(
          s"""role("dev")""",
          s"""operation("read")""",
        ),
        resources = Seq.empty,
        rules = Seq.empty,
        policies = Seq(
          s"""allow if true""",
        ),
        revokedIds = Seq.empty,
      ),
      extractor = BiscuitExtractorConfig()
    )

    // Create entities
    leaderClient.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    leaderClient.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(validator)
    await(5.seconds)

    val routeDomain = s"${UUID.randomUUID().toString}.oto.tools"

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
              validator.id
            ),
            "enforce" -> true
          ))
        )
      ))
    )

    leaderClient.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeWithVerifier)
    await(3.seconds)

    val headers = Map(
      "Authorization" -> token
    )

    val respCallVerifier = leaderClient.call("POST", s"http://${routeDomain}:${leaderPort}", headers, None).awaitf(5.seconds)

    assertEquals(respCallVerifier.status, 403, s"verifier route should be forbidden")
    assert(respCallVerifier.json.at("Otoroshi-Error").isDefined, s"error message should be defined")
    assertEquals(respCallVerifier.json.at("Otoroshi-Error").as[String], "Token is revoked", s"error message should be Token is revoked")
  }
}