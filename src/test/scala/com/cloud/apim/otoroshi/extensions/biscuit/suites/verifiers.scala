package com.cloud.apim.otoroshi.extensions.biscuit.suites

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.domains.{BiscuitKeyPairsUtils, BiscuitVerifiersUtils}
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitKeyPair, BiscuitVerifier, VerifierConfig}
import com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionSuite, OtoroshiClient}
import otoroshi.api.Otoroshi
import otoroshi.models.EntityLocation
import otoroshi.utils.syntax.implicits._
import play.api.libs.json.{JsObject, Json}

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class TestVerifiers extends BiscuitExtensionSuite {
  def testVerifierWithoutRef(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    val routeVerifierId = s"route_${UUID.randomUUID().toString}"

    val routeWithoutVerifier = client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeVerifierId, Json.parse(
    s"""{
         |  "id": "${routeVerifierId}",
         |  "name": "biscuit-verifier",
         |  "frontend": {
         |    "domains": [
         |      "verifier.oto.tools"
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
         |        "verifier_ref": "",
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
    assert(routeWithoutVerifier.created, s"verifier route has not been created")
    await(1300.millis)


    val resp = client.call("GET", s"http://verifier.oto.tools:${port}", Map.empty, None).awaitf(awaitFor)
    assertEquals(resp.status, 500, s"verifier did not thrown an error 500")
    assert(resp.json.at("error").isDefined, s"error is not defined")
    assertEquals(resp.json.at("error").as[String], "verifierRef not found", s"bad error message for verifier route")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    await(1000.millis)
  }

  def testBiscuitVerifier(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    // user("biscuit-studio-test");
    // role("user");
    val encodedToken = "EpYBCiwKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIIggKBggGEgIYChIkCAASIIjCXLU5A-JBhCzBWklpKOr4azUhMLMXQzrhcLXTqxsfGkA6aVZxS-uu8tpQtbK_NxeaGxpc-WPBYPgO83NZQZSdLbE4ELcfgn-6OoH-jp6Ych7M_T3t1vBoNnSp4Paah9UHIiIKIBvwbLEZSZUtv2sQCY_UUBI-wBjBk9gnHXW4uQRGHzSv"

    val keypairID = s"biscuit-keypair_53105273-61db-4791-a8cf-04e98b5a2c12"
    val verifierId = s"biscuit-verifier_603ad95d-b723-4883-817a-f9c739e157b8"
    val routeVerifierId = s"route_${UUID.randomUUID().toString}"

    val demoKeyPair = BiscuitKeyPair(
      id = keypairID,
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      pubKey = "0D6C7CBAECBA63916D7CAE5981C411EAF6A18929910709977FFDDFBD4433EF27",
      privKey = "1304480d1d9eea4075de296d8d84c2522703778c01b0a1459145716a7c33a665"
    )

    BiscuitKeyPairsUtils.createKeypairEntity(client)(demoKeyPair)

    val conf = VerifierConfig(
      checks = List.empty,
      facts = List.empty,
      resources = List.empty,
      rules = List.empty,
      policies = Seq("allow if user(\"biscuit-studio-test\");"),
      revokedIds = List.empty
    )

    val verifier = BiscuitVerifier(
      id = verifierId,
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypairID,
      config =  conf.some
    )

    BiscuitVerifiersUtils.createVerifierEntity(client)(verifier)

    val routeWithVerifier = client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeVerifierId, Json.parse(
      s"""{
         |  "id": "${routeVerifierId}",
         |  "name": "biscuit-verifier",
         |  "frontend": {
         |    "domains": [
         |      "verifier.oto.tools"
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
    await(1300.millis)

    val headers = Map(
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://verifier.oto.tools:${port}", headers, None).awaitf(awaitFor)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    client.forBiscuitEntity("biscuit-verifiers").deleteRaw(verifierId)
    client.forBiscuitEntity("biscuit-keypairs").deleteRaw(keypairID)

    await(1300.millis)
  }

  def testBiscuitVerifierWithBadToken(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    // user("biscuit-studio-test");
    // role("user");
    val encodedToken = "EcYBCiwKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIIggKBggGEgIYChIkCAASIIjCXLU5A-JBhCzBWklpKOr4azUhMLMXQzrhcLXTqxsfGkA6aVZxS-uu8tpQtbK_NxeaGxpc-WPBYPgO83NZQZSdLbE4ELcfgn-6OoH-jp6Ych7M_T3t1vBoNnSp4Paah9UHIiIKIBvwbLEZSZUtv2sQCY_UUBI-wBjBk9gnHXW4uQRGHzSv"

    val keypairID = s"biscuit-keypair_53105273-61db-4791-a8cf-04e98b5a2c12"
    val verifierId = s"biscuit-verifier_603ad95d-b723-4883-817a-f9c739e157b8"
    val routeVerifierId = s"route_${UUID.randomUUID().toString}"

    val demoKeyPair = BiscuitKeyPair(
      id = keypairID,
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      pubKey = "0D6C7CBAECBA63916D7CAE5981C411EAF6A18929910709977FFDDFBD4433EF27",
      privKey = "1304480d1d9eea4075de296d8d84c2522703778c01b0a1459145716a7c33a665"
    )

    BiscuitKeyPairsUtils.createKeypairEntity(client)(demoKeyPair)

    val conf = VerifierConfig(
      checks = List.empty,
      facts = List.empty,
      resources = List.empty,
      rules = List.empty,
      policies = Seq("allow if user(\"biscuit-studio-test\");"),
      revokedIds = List.empty
    )

    val verifier = BiscuitVerifier(
      id = verifierId,
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypairID,
      config =  conf.some
    )

    BiscuitVerifiersUtils.createVerifierEntity(client)(verifier)

    val routeWithVerifier = client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeVerifierId, Json.parse(
      s"""{
         |  "id": "${routeVerifierId}",
         |  "name": "biscuit-verifier",
         |  "frontend": {
         |    "domains": [
         |      "verifier.oto.tools"
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
    assert(routeWithVerifier.created, s"verifier route chat has not been created")
    await(1300.millis)

    val headers = Map(
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://verifier.oto.tools:${port}", headers, None).awaitf(awaitFor)
    println(resp.body)
    assertEquals(resp.status, 500, s"verifier should thrown an internal server error")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    client.forBiscuitEntity("biscuit-verifiers").deleteRaw(verifierId)
    client.forBiscuitEntity("biscuit-keypairs").deleteRaw(keypairID)
    await(1300.millis)
  }

  def testWrongBiscuitTokenWithVerifier(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    // user("biscuit-studio-test");
    // role("user");
    val encodedToken = "EpYBCiwKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIIggKBggGEgIYChIkCAASIIjCXLU5A-JBhCzBWklpKOr4azUhMLMXQzrhcLXTqxsfGkA6aVZxS-uu8tpQtbK_NxeaGxpc-WPBYPgO83NZQZSdLbE4ELcfgn-6OoH-jp6Ych7M_T3t1vBoNnSp4Paah9UHIiIKIBvwbLEZSZUtv2sQCY_UUBI-wBjBk9gnHXW4uQRGHzSv"

    val keypairID = s"biscuit-keypair_9cf71a29-303e-40f2-88d0-b86805a73de0"
    val verifierId = s"biscuit-verifier_40770e97-825c-4abc-9f92-0051fa2eff67"
    val routeVerifierId = s"route_${UUID.randomUUID().toString}"

    val demoKeyPair = BiscuitKeyPair(
      id = keypairID,
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      pubKey = "0D6C7CBAECBA63916D7CAE5981C411EAF6A18929910709977FFDDFBD4433EF27",
      privKey = "1304480d1d9eea4075de296d8d84c2522703778c01b0a1459145716a7c33a665"
    )

    BiscuitKeyPairsUtils.createKeypairEntity(client)(demoKeyPair)

    val conf = VerifierConfig(
      checks = List.empty,
      facts = List.empty,
      resources = List.empty,
      rules = List.empty,
      policies = Seq("allow if user(\"biscuit-studio-test-2\");"),
      revokedIds = List.empty
    )

    val verifier = BiscuitVerifier(
      id = verifierId,
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypairID,
      config =  conf.some
    )

    BiscuitVerifiersUtils.createVerifierEntity(client)(verifier)

    val routeDomain = "verifier.oto.tools"

    val routeWithVerifier = client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeVerifierId, Json.parse(
      s"""{
         |  "id": "${routeVerifierId}",
         |  "name": "biscuit-verifier-2",
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
    assert(routeWithVerifier.created, s"verifier route chat has not been created")
    await(1300.millis)

    val headers = Map(
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(awaitFor)
    assertEquals(resp.status, 403, s"verifier should thrown a forbidden")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    client.forBiscuitEntity("biscuit-verifiers").deleteRaw(verifierId)
    client.forBiscuitEntity("biscuit-keypairs").deleteRaw(keypairID)
    await(1300.millis)
  }

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

  val entityId = s"biscuit-verifier_7086fb05-0a0b-4be8-92b0-1f89ab243a83"

  val conf = VerifierConfig(
    checks = List.empty,
    facts = List.empty,
    resources = List.empty,
    rules = List.empty,
    policies = List.empty,
    revokedIds = List.empty
  )

  val verifier = BiscuitVerifier(
    id = entityId,
    name = "New Biscuit Verifier entity",
    description = "New biscuit Verifier entity",
    metadata = Map.empty,
    tags = Seq.empty,
    location = EntityLocation.default,
    keypairRef = "",
    config =  conf.some
  )

  test(s"create verifier entity") {
    printHeader(verifier.name, "Create new verifier entity")
    BiscuitVerifiersUtils.createVerifierEntity(client)(verifier)
  }

  test(s"verifier plugin should throw an error - verifier not provided") {
    printHeader(verifier.name,  "test verifier plugin without verifier_ref")
    testVerifierWithoutRef(client, 30.seconds)
  }

  test(s"testing good verifier plugin policies") {
    printHeader(verifier.name, "testing good verifier plugin policies")
    testBiscuitVerifier(client, 30.seconds)
  }

  test(s"testing WRONG token plugin") {
    printHeader("", "testing WRONG token plugin")
    testWrongBiscuitTokenWithVerifier(client, 30.seconds)
  }

  test(s"testing verifier plugin with bad token") {
    printHeader(verifier.name,  "testing verifier plugin with bad token")
    testBiscuitVerifierWithBadToken(client, 30.seconds)
  }
}
