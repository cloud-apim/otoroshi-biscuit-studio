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
    await(1500.millis)


    val resp = client.call("GET", s"http://verifier.oto.tools:${port}", Map.empty, None).awaitf(awaitFor)
    assertEquals(resp.status, 500, s"verifier did not thrown an error 500")
    assert(resp.json.at("error").isDefined, s"error is not defined")
    assertEquals(resp.json.at("error").as[String], "verifierRef not found", s"bad error message for verifier route")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    await(2500.millis)
  }

  def testBiscuitVerifier(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    // user("biscuit-studio-test");
    // role("user");
    val encodedToken = "EowBCiIKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIEiQIABIg1J7SCEMRkgB3g7W5W-99Xatm0w3CJkWpFhYPxKxQQFsaQCulBZ8u-x6C8h9BdYEPCmEJhzKPC3n3SH-Hka55A6PsYhvcSeB6nJbqLWT61T8Gvu0V_UHwjlYYAkkAfaLgegQiIgogaTlcTS0Wrl6hw7ewN_41AWoDd3gbw3KvYGrwHA3kmfI="

    val keypairID = s"biscuit-keypair_53105273-61db-4791-a8cf-04e98b5a2c12"
    val verifierId = s"biscuit-verifier_603ad95d-b723-4883-817a-f9c739e157b8"
    val routeVerifierId = s"route_${UUID.randomUUID().toString}"

    val routeDomain = s"verifier-${UUID.randomUUID().toString}.oto.tools"

    val demoKeyPair = BiscuitKeyPair(
      id = keypairID,
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      pubKey = "C6895E147596F71984C6F2D70005A7915658C0076C95BA45FE34B0EB2541487B",
      privKey = "DDCC600AE43F2C7E1FED87EEA7C6CBB7F9539FE6166D0A73A4FAFDD1AE4BFDD7"
    )

    BiscuitKeyPairsUtils.createKeypairEntity(client)(demoKeyPair)

    val conf = VerifierConfig(
      checks = List.empty,
      facts = List.empty,
      resources = List.empty,
      rules = List.empty,
      policies = Seq(
        "allow if user(\"biscuit-studio-test\");"
      ),
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
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(awaitFor)
    println("body = ", resp.body)
    println("status = ", resp.status)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(demoKeyPair)

    await(2500.millis)
  }

  def testBiscuitVerifierWithBadToken(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    // user("biscuit-studio-test");
    // role("user");
    val encodedToken = "EcYBCiwKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIIggKBggGEgIYChIkCAASIIjCXLU5A-JBhCzBWklpKOr4azUhMLMXQzrhcLXTqxsfGkA6aVZxS-uu8tpQtbK_NxeaGxpc-WPBYPgO83NZQZSdLbE4ELcfgn-6OoH-jp6Ych7M_T3t1vBoNnSp4Paah9UHIiIKIBvwbLEZSZUtv2sQCY_UUBI-wBjBk9gnHXW4uQRGHzSv"

    val keypairID = s"biscuit-keypair_072970c2-fb59-4757-aa27-ec6778702bfc"
    val verifierId = s"biscuit-verifier_be4e215e-41e3-4d48-8cb5-e582347ccf78"
    val routeVerifierId = s"route_${UUID.randomUUID().toString}"
    val routeDomain = s"verifier-${UUID.randomUUID().toString}.oto.tools"

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
    await(1500.millis)

    val headers = Map(
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(awaitFor)
    println(resp.body)
    assertEquals(resp.status, 500, s"verifier should thrown an internal server error")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(demoKeyPair)

    await(2500.millis)
  }

  def testWrongBiscuitTokenWithVerifier(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    // user("biscuit-studio-test");
    // role("user");
    val encodedToken = "EpYBCiwKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIIggKBggGEgIYChIkCAASIIjCXLU5A-JBhCzBWklpKOr4azUhMLMXQzrhcLXTqxsfGkA6aVZxS-uu8tpQtbK_NxeaGxpc-WPBYPgO83NZQZSdLbE4ELcfgn-6OoH-jp6Ych7M_T3t1vBoNnSp4Paah9UHIiIKIBvwbLEZSZUtv2sQCY_UUBI-wBjBk9gnHXW4uQRGHzSv"

    val keypairID = s"biscuit-keypair_9cf71a29-303e-40f2-88d0-b86805a73de0"
    val verifierId = s"biscuit-verifier_40770e97-825c-4abc-9f92-0051fa2eff67"
    val routeVerifierId = s"route_${UUID.randomUUID().toString}"

    val routeDomain = s"verifier-${UUID.randomUUID().toString}.oto.tools"

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
    await(1500.millis)

    val headers = Map(
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(awaitFor)
    println("body = ", resp.body)
    println("status = ", resp.status)
    assertEquals(resp.status, 403, s"verifier should thrown a forbidden")
    assert(resp.json.at("Otoroshi-Error").isDefined, s"body error is not defined")
    assertEquals(resp.json.at("Otoroshi-Error").asString, "forbidden", s"body error wrong message")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(demoKeyPair)
    await(2500.millis)
  }

  def testRoleAdmin(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    // user("biscuit-studio-test");
    // role("admin");
    val encodedToken = "EpYBCiwKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIIggKBggGEgIYDRIkCAASIJPomfHlj_GI42mey6GrubIhTCtgPLpa0fMgRMjTd9JeGkDzTqX8amadGEmHA8AvtvzvQkULKkUf5UNUnh8MB5BlOQHUiYu11qrRE1Ky0kUcsYswGvRWRypdXRwaj6rDRkoEIiIKIIPyLZyNKi8bhgx0-9nXn8U-27zEumu6wMHhz9LrKDDk"

    val keypairID = s"biscuit-keypair_d0807da0-3e09-4e0d-b7d4-bf59a71abe47"
    val verifierId = s"biscuit-verifier_10b6ee8d-8704-474c-84a6-57fb17774a35"
    val routeVerifierId = s"route_${UUID.randomUUID().toString}"
    val routeDomain = s"verifier-${UUID.randomUUID().toString}.oto.tools"

    val demoKeyPair = BiscuitKeyPair(
      id = keypairID,
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      pubKey = "A83F44B969CB64074D1BABBB3F2F73CE86E74A782C9FA191528D797967F1A352",
      privKey = "ECD87DAF9153424EA7BBF3C55BE4ED000CEB78B102C4FB930BBEA159F3983E0A"
    )

    BiscuitKeyPairsUtils.createKeypairEntity(client)(demoKeyPair)

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
      "biscuit-header" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(awaitFor)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(demoKeyPair)

    await(2500.millis)
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

  test(s"testing role admin") {
    printHeader(verifier.name,  "testing role admin")
    testRoleAdmin(client, 30.seconds)
  }
}
