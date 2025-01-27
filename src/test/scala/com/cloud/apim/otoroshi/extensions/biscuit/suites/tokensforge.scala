package com.cloud.apim.otoroshi.extensions.biscuit.suites

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.domains.{BiscuitKeyPairsUtils, BiscuitTokensForgeUtils, BiscuitVerifiersUtils}
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitKeyPair, BiscuitTokenForge, BiscuitVerifier, VerifierConfig}
import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitForgeConfig
import com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionSuite, OtoroshiClient}
import otoroshi.api.Otoroshi
import otoroshi.models.EntityLocation
import otoroshi.utils.syntax.implicits._
import play.api.libs.json.{JsObject, Json}

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class TestsTokensForge extends BiscuitExtensionSuite {
  def printHeader(str: String, what: String): Unit = {
    println("\n\n-----------------------------------------")
    println(s"  [${str}] - ${what}")
    println("-----------------------------------------\n\n")
  }

  val port: Int = freePort
  var otoroshi: Otoroshi = _
  var client: OtoroshiClient = _
  implicit var ec: ExecutionContext = _
  implicit var mat: Materializer = _

  override def beforeAll(): Unit = {
    otoroshi = startOtoroshiServer(port)
    client = clientFor(port)
    ec = otoroshi.executionContext
    mat = otoroshi.materializer
  }

  override def afterAll(): Unit = {
    otoroshi.stop()
  }

  val entityId = s"biscuit-token_5042ae60-418d-4a92-9d50-57787b8970f5"
  val keypairID = s"biscuit-keypair_5a2ec0f9-35cd-48b2-9233-c024b5074df3"
  val tokenDemo = "EpYBCiwKDGJpc2N1aXQtZGVtbwoEMTIzNBgDIgkKBwgKEgMYgAgiCQoHCAYSAxiBCBIkCAASIB1ARKTqCEIC_2wnoCP78zyMUKhdWjEOHGOEHByZEUPkGkCAbpHaQKDBYKpc3GeiF40nZwsANGS-pSU3o9h-glQj3lF0Y1TtouAv6C7ur5siywAr-IR_1FMasRAPfAiXuZgDIiIKIOsz8F2Y43MgQVTVRJchzeDuZk8rRarjdTFLH_0dITub"

  val demoKeyPair = BiscuitKeyPair(
    id = keypairID,
    name = "New Biscuit Key Pair",
    description = "New biscuit KeyPair",
    metadata = Map.empty,
    tags = Seq.empty,
    location = EntityLocation.default,
    pubKey = "d8667526cc5e2e3fd4822571a68d815f8c8f6128edfcfe6701e7d76219db9e29",
    privKey = "998b3813a64845ff437acdaebd91bd63f83c6fc7ebdaad03c894b1b6a164f0d2"
  )

  val conf = BiscuitForgeConfig(
    checks = List.empty,
    facts = Seq(
      "user(\"biscuit-demo\");",
      "role(\"1234\");",
    ),
    resources = List.empty,
    rules = List.empty
  )

  val biscuitToken = BiscuitTokenForge(
    id = entityId,
    name = "New Biscuit Token entity",
    description = "New biscuit Token entity",
    metadata = Map.empty,
    tags = Seq.empty,
    location = EntityLocation.default,
    keypairRef = keypairID,
    config =  conf.some,
  )

  def testWithVerifier(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    val verifierId = s"biscuit-verifier_${UUID.randomUUID().toString}"
    val routeVerifierId = s"route_${UUID.randomUUID().toString}"
    val routeDomain = s"verifier-${UUID.randomUUID().toString}.oto.tools"

    val verifier = BiscuitVerifier(
      id = verifierId,
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypairID,
      config =   VerifierConfig(
        checks = List.empty,
        facts = Seq(
          "operation(\"read\");",
          "resource(\"1234\");",
        ),
        resources = Seq(
          "/folder1/file1"
        ),
        rules = List.empty,
        policies = Seq(
          "allow if role(\"1234\");"
        ),
        revokedIds = List.empty
      ).some
    )

    BiscuitVerifiersUtils.createVerifierEntity(client)(verifier)

    val routeWithVerifier = client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeVerifierId, Json.parse(
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
         |    {
         |      "enabled": true,
         |      "debug": false,
         |      "plugin": "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenValidator",
         |      "include": [],
         |      "exclude": [],
         |      "config": {
         |        "verifier_ref": "${verifierId}",
         |        "rbac_ref": "",
         |        "enable_remote_facts": false,
         |        "remote_facts_ref": "",
         |        "enforce": true,
         |        "extractor_type": "header",
         |        "extractor_name": "biscuit-header"
         |      },
         |      "bound_listeners": [],
         |      "plugin_index": {
         |        "validate_access": 0
         |      }
         |    }
         |  ]
         |}""".stripMargin)).awaitf(awaitFor)
    assert(routeWithVerifier.created, s"verifier route has not been created")
    await(1500.millis)

    val headers = Map(
      "biscuit-header" -> tokenDemo
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(awaitFor)
    println("body = ", resp.body)
    println("status = ", resp.status)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)

    await(2500.millis)
  }

  test(s"create token from forge entity") {
    printHeader(demoKeyPair.name, "Create Keypair")
    BiscuitKeyPairsUtils.createKeypairEntity(client)(demoKeyPair)
    printHeader(biscuitToken.name, "Create biscuit token entity")
    BiscuitTokensForgeUtils.createTokenEntity(client)(biscuitToken)
    printHeader("", "Testing the biscuit entity with a verifier")
    testWithVerifier(client, 30.seconds)
  }
}
