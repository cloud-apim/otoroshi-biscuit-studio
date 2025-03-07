package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiClusterPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitExtractorConfig, BiscuitKeyPair, BiscuitVerifier, VerifierConfig}
import org.joda.time.DateTime
import otoroshi.models.EntityLocation
import otoroshi.next.models.{NgBackend, NgDomainAndPath, NgFrontend, NgPluginInstance, NgPluginInstanceConfig, NgPlugins, NgRoute, NgTarget}
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits.{BetterFuture, BetterJsValue}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenValidator
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.duration.DurationInt

class RevocationSuite extends BiscuitStudioOneOtoroshiClusterPerSuite {

  test("should revoke on all cluster"){

      val token = "Eo0BCiMKBDEyMzQYAyIJCgcIChIDGIAIMg4KDAoCCBsSBggDEgIYABIkCAASIO5XgWOCNcmU7hLSWg5kl9CcJuOfGg049qB6Msxi43MQGkAaT-TlhUULsSNCdhJmM-Ixl-VNCwnhiP3Ijjc0RiSb6uJOeDOxl0bmKlg6kNb6bygZCAtFvnfbC7scMzvAfxEHGoIBChgKA2RldhgDMg8KDQoCCBsSBwgGEgMYgQgSJAgAEiD73BEy0MywJgKQRGileTkSqOHIoMUiMZHMtoxNpE9AaxpAMon9LKykhc0YwHi6V5hUaI9IEYdLGIr36bPoSFhtZrHqnRzQqa5jdkPScvPWvoyct1t4gop4DJ-KLDYWYCBoCCIiCiAnReFaEoEjZtLxePwTKU7KbnvIW9_v0WlJvcMp44mMTg=="
      val revocationId = "3289fd2caca485cd18c078ba579854688f4811874b188af7e9b3e848586d66b1ea9d1cd0a9ae637643d272f3d6be8c9cb75b78828a780c9f8a2c361660206808"
    val revokedTokenBody = Some(Json.arr(
      Json.obj(
        "id" -> revocationId,
        "reason" -> "fraud",
        "revocation_date" -> DateTime.now().toString()
      ))
    )

    // Used token
    // Eo0BCiMKBDEyMzQYAyIJCgcIChIDGIAIMg4KDAoCCBsSBggDEgIYABIkCAASIHS1xEsEbwjjsW390Tu0Iz6JWHB_XVQD9eCq9pncR3zYGkDx1E1fOsbnQVWqJLClKouVgSz7zWto0zUB8Y01GjUi9IrOTZHlkl6zpdoiqt5Xml17yK2M-egQNbLTvYRMw3cFGoIBChgKA2RldhgDMg8KDQoCCBsSBwgGEgMYgQgSJAgAEiAkqKaZsLUXlWjGQ6AHcTWa22JLhVBSAaatzAhfTjMcyBpA1GzXb8cYiUvCEDbuTKywAWMs-mIGOJ4u4uKQqzREO--FrikYnQ6OYjbVCuAQ1lvrPF0QsnjgJtSCgAiAK-0ZDCIiCiCFDocKFnhkO3y5aHFVDYsow0YitpllvSoeFlr50Fm40Q==


    // Call worker 2 api to revoke a token
    val resRevocation = workerClient2.call("POST", s"http://otoroshi-api.oto.tools:${workerPort2}/api/extensions/biscuit/tokens/revocation/_revoke",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), revokedTokenBody).awaitf(5.seconds)


    assertEquals(resRevocation.status, 200, "status should be 200")
    assert(resRevocation.json.at("total_revoked").isDefined, "total_revoked should be defined")
    assertEquals(resRevocation.json.at("total_revoked").as[Int], 1, "total_revoked nb should be 1")

    await(5.seconds)
    var allRvkTokensWrk1 = envWorker.adminExtensions.extension[BiscuitExtension].get.states.allRevokedTokens()
    var allRvkTokensWrk2 = envWorker2.adminExtensions.extension[BiscuitExtension].get.states.allRevokedTokens()
    var allRvkTokensLeader = env.adminExtensions.extension[BiscuitExtension].get.states.allRevokedTokens()

    println(s"allRvkTokens WRK1 = ${allRvkTokensWrk1}")
    println(s"allRvkTokens LEAD = ${allRvkTokensLeader}")
    println(s"allRvkTokens WRK2 = ${allRvkTokensWrk2}")

//    assertEquals(allRvkTokensLeader.size, 0, "revoked tokens should be empty on worker 2")
//    assertEquals(allRvkTokensLeader.size, 0, "revoked tokens should be empty on cluster leader")
//    assertEquals(allRvkTokensWrk1.size, 1, "revoked tokens should not be empty on worker 1")

    await(20.seconds)

    allRvkTokensWrk2 = envWorker2.adminExtensions.extension[BiscuitExtension].get.states.allRevokedTokens()
    println(s"allRvkTokens size = ${allRvkTokensWrk2.size}")
    println(s"allRvkTokens = ${allRvkTokensWrk2}")

    val revTokenWrk2 =  envWorker2.adminExtensions.extension[BiscuitExtension].get.states.biscuitRevokedTokens(revocationId)
    val revTokenLeader =  envWorker2.adminExtensions.extension[BiscuitExtension].get.states.biscuitRevokedTokens(revocationId)

    println(s"got revtoken on worker = ${revTokenWrk2}")
    println(s"got revtoken on leader = ${revTokenLeader}")

    assert(revTokenWrk2.isDefined, "token should be defined")
    assertEquals(revTokenWrk2.get.revocationId, revocationId, "token id should not be wrong")

    assert(revTokenLeader.isDefined, "token should be defined")
    assertEquals(revTokenLeader.get.revocationId, revocationId, "token id should not be wrong")

    // Call a verifier plugin with the revoked token
    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", envWorker2),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = "4379BE5B9AFA1A84F59D2417C20020EF1E47E0805945535B45616209D8867E50",
      pubKey = "771F9E7FE62784502FE34CE862220586D3DB637D6A5ABAD254F7330369D3B357"
    )

    val validator = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit-verifier", envWorker2),
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
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath("verifier.oto.tools")), stripPath = false),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(NgPluginInstance(
        plugin = s"cp:${classOf[BiscuitTokenValidator].getName}",
        config = NgPluginInstanceConfig(Json.obj(
          "verifier_refs" -> Seq(validator.id)
        )))
      ))
    )

    leaderClient.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    leaderClient.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-verifiers").upsertEntity(validator)

    await(5.seconds)

    val routeVerifierId = s"${UUID.randomUUID().toString}"
    val routeDomain = s"${UUID.randomUUID().toString}.oto.tools"

    val routeWithVerifier = leaderClient.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeVerifierId, Json.parse(
      s"""{
         |  "id": "${routeVerifierId}",
         |  "name": "biscuit-verifier",
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
         |     {
         |      "plugin_index": {},
         |      "nodeId": "cp:otoroshi.next.plugins.EchoBackend-0",
         |      "plugin": "cp:otoroshi.next.plugins.EchoBackend",
         |      "enabled": true,
         |      "debug": false,
         |      "include": [],
         |      "exclude": [],
         |      "bound_listeners": [],
         |      "config": {
         |        "limit": 524288
         |      }
         |    },
         |    {
         |      "enabled": true,
         |      "debug": false,
         |      "plugin": "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenValidator",
         |      "include": [],
         |      "exclude": [],
         |      "config": {
         |        "verifier_refs": ["${validator.id}"],
         |        "enforce": true
         |      },
         |      "bound_listeners": [],
         |      "plugin_index": {
         |        "validate_access": 0
         |      }
         |    }
         |  ]
         |}""".stripMargin)).awaitf(2.seconds)

    println(s"routeWithVerifier = ${routeWithVerifier.bodyJson}")

    assert(routeWithVerifier.created, s"verifier route has not been created")
    await(2.seconds)

    val headers = Map(
      "Authorization" -> token
    )

    val respCallVerifier = leaderClient.call("POST", s"http://${routeDomain}:${leaderPort}", headers, None).awaitf(5.seconds)

    assertEquals(respCallVerifier.status, 403, s"verifier route should be forbidden")
    assert(respCallVerifier.json.at("Otoroshi-Error").isDefined, s"error message should be defined")
    assertEquals(respCallVerifier.json.at("Otoroshi-Error").as[String], "DeserializationError - Token is revoked", s"error message should be Token is revoked")
  }
}