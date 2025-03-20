package com.cloud.apim.otoroshi.extensions.biscuit.suites

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.domains.{BiscuitAttenuatorsUtils, BiscuitKeyPairsUtils}
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{AttenuatorConfig, BiscuitAttenuator, BiscuitExtractorConfig, BiscuitKeyPair}
import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitUtils
import com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionSuite, OtoroshiClient}
import org.biscuitsec.biscuit.crypto.PublicKey
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.api.Otoroshi
import otoroshi.models.EntityLocation
import otoroshi.utils.syntax.implicits._
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.CollectionConverters._

class TestAttenuators extends BiscuitExtensionSuite {
  val port: Int = freePort
  val entityTestId = s"biscuit-attenuator_${UUID.randomUUID().toString}"
  val attenuatorTest = BiscuitAttenuator(
    id = entityTestId,
    name = "New Biscuit Attenuator entity",
    description = "New biscuit Attenuator entity",
    metadata = Map.empty,
    tags = Seq.empty,
    location = EntityLocation.default,
    keypairRef = "",
    config = AttenuatorConfig(
      checks = List.empty,
    )
  )
  implicit var ec: ExecutionContext = _
  implicit var mat: Materializer = _
  var otoroshi: Otoroshi = _
  var client: OtoroshiClient = _

  def testAttenuatorWithoutRef(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    val routeVerifierId = s"route_${UUID.randomUUID().toString}"
    val routeDomain = "attenuator-wrong-ref.oto.tools"

    val routeWithoutVerifier = client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeVerifierId, Json.parse(
      s"""{
         |  "id": "${routeVerifierId}",
         |  "name": "biscuit-attenuator",
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
         |      "plugin": "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenAttenuator",
         |      "include": [],
         |      "exclude": [],
         |      "config": {
         |        "attenuator_ref": "",
         |        "extractor_type": "header",
         |        "extractor_name": "Authorization",
         |        "token_replace_loc": "query",
         |        "token_replace_name": "auth"
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


    val resp = client.call("GET", s"http://${routeDomain}:${port}", Map.empty, None).awaitf(awaitFor)
    assertEquals(resp.status, 500, s"verifier did not thrown an error 500")
    assert(resp.json.at("error").isDefined, s"error is not defined")
    assertEquals(resp.json.at("error").as[String], "attenuator_ref not found in your plugin configuration", s"bad error message for verifier route")
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeVerifierId)
    await(2500.millis)
  }

  def testAttenuatorPluginHeaders(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    // user("biscuit-studio-test");
    // role("user");
    val encodedToken = "EpYBCiwKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIIggKBggGEgIYChIkCAASIIjCXLU5A-JBhCzBWklpKOr4azUhMLMXQzrhcLXTqxsfGkA6aVZxS-uu8tpQtbK_NxeaGxpc-WPBYPgO83NZQZSdLbE4ELcfgn-6OoH-jp6Ych7M_T3t1vBoNnSp4Paah9UHIiIKIBvwbLEZSZUtv2sQCY_UUBI-wBjBk9gnHXW4uQRGHzSv"

    val keypairId = "biscuit-keypair_65c6160d-c22a-41a9-a351-b97ef714ca96"
    val attenuatorId = "biscuit-attenuator_3e244291-d0ff-48cc-9584-5c0144af4f2c"
    val routeId = s"route_${UUID.randomUUID().toString}"
    val routeDomain = "attenuator-headers.oto.tools"

    val publicKey = "0D6C7CBAECBA63916D7CAE5981C411EAF6A18929910709977FFDDFBD4433EF27"

    val demoKeyPair = BiscuitKeyPair(
      id = keypairId,
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      pubKey = publicKey,
      privKey = "1304480d1d9eea4075de296d8d84c2522703778c01b0a1459145716a7c33a665"
    )

    BiscuitKeyPairsUtils.createKeypairEntity(client)(demoKeyPair)
    val publicKeyFormatted = demoKeyPair.getPubKey

    val conf = AttenuatorConfig(
      checks = Seq("check if time($date), $date >= 2025-12-30T19:00:10Z;"),
    )

    val attenuator = BiscuitAttenuator(
      id = attenuatorId,
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypairId,
      config = conf
    )

    BiscuitAttenuatorsUtils.createAttenuatorEntity(client)(attenuator)

    val routeWithAttenuator = client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeId, Json.parse(
      s"""{
         |  "id": "${routeId}",
         |  "name": "biscuit-attenuator",
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
         |"plugins": [
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
         |      "plugin": "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenAttenuator",
         |      "include": [],
         |      "exclude": [],
         |      "config": {
         |        "attenuator_ref": "${attenuatorId}",
         |        "extractor_type": "header",
         |        "extractor_name": "biscuit-token-test",
         |        "token_replace_loc": "header",
         |        "token_replace_name": "biscuit-attenuated-token"
         |      },
         |      "bound_listeners": [],
         |      "plugin_index": {
         |        "validate_access": 0
         |      }
         |    }
         |  ]
         |}""".stripMargin)).awaitf(awaitFor)
    assert(routeWithAttenuator.created, s"attenuator route has not been created")
    await(1500.millis)

    val headers = Map(
      "biscuit-token-test" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(awaitFor)

    assertEquals(resp.status, 200, s"attenuator route did not respond with 200")
    assert(resp.json.at("headers.biscuit-attenuated-token").isDefined, s"response headers don't contains the biscuit attenuated token")

    val attenuatedToken = BiscuitExtractorConfig.replaceHeader(resp.json.at("headers.biscuit-attenuated-token").get.asString)
    assert(attenuatedToken.nonEmpty, s"attenuated token is empty")

    val encodedBiscuit = Biscuit.from_b64url(attenuatedToken, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, conf.checks.size, s"attenuated token doesn't contain checks list")

    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeId)
    client.forBiscuitEntity("biscuit-attenuators").deleteEntity(attenuator)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(demoKeyPair)
    await(2500.millis)
  }

  def testAttenuatorPluginCookie(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    // user("biscuit-studio-test");
    // role("user");
    val encodedToken = "EpYBCiwKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIIggKBggGEgIYChIkCAASIIjCXLU5A-JBhCzBWklpKOr4azUhMLMXQzrhcLXTqxsfGkA6aVZxS-uu8tpQtbK_NxeaGxpc-WPBYPgO83NZQZSdLbE4ELcfgn-6OoH-jp6Ych7M_T3t1vBoNnSp4Paah9UHIiIKIBvwbLEZSZUtv2sQCY_UUBI-wBjBk9gnHXW4uQRGHzSv"

    val keypairId = "biscuit-keypair_9f37c002-5d42-4a3c-bcda-f7317d8087a7"
    val attenuatorId = "biscuit-attenuator_788d0092-9cab-407d-bd80-d3fca76c144e"
    val routeId = s"route_${UUID.randomUUID().toString}"
    val routeDomain = "attenuator-cookies.oto.tools"

    val publicKey = "0D6C7CBAECBA63916D7CAE5981C411EAF6A18929910709977FFDDFBD4433EF27"

    val demoKeyPair = BiscuitKeyPair(
      id = keypairId,
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      pubKey = publicKey,
      privKey = "1304480d1d9eea4075de296d8d84c2522703778c01b0a1459145716a7c33a665"
    )

    BiscuitKeyPairsUtils.createKeypairEntity(client)(demoKeyPair)

    val conf = AttenuatorConfig(
      checks = Seq("check if time($date), $date >= 2025-12-30T19:00:10Z;"),
    )

    val attenuator = BiscuitAttenuator(
      id = attenuatorId,
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypairId,
      config = conf
    )

    BiscuitAttenuatorsUtils.createAttenuatorEntity(client)(attenuator)

    val routeWithAttenuator = client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeId, Json.parse(
      s"""{
         |  "id": "${routeId}",
         |  "name": "biscuit-attenuator",
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
         |"plugins": [
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
         |      {
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
         |      "plugin": "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenAttenuator",
         |      "include": [],
         |      "exclude": [],
         |      "config": {
         |        "attenuator_ref": "${attenuatorId}",
         |        "extractor_type": "header",
         |        "extractor_name": "biscuit-token-test",
         |        "token_replace_loc": "cookie",
         |        "token_replace_name": "biscuit-attenuated-token"
         |      },
         |      "bound_listeners": [],
         |      "plugin_index": {
         |        "validate_access": 0
         |      }
         |    }
         |  ]
         |}""".stripMargin)).awaitf(awaitFor)
    assert(routeWithAttenuator.created, s"attenuator route has not been created")
    await(1300.millis)

    val headers = Map(
      "biscuit-token-test" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(awaitFor)

    assertEquals(resp.status, 200, s"attenuator route did not respond with 200")
    assert(resp.json.at("cookies.biscuit-attenuated-token").isDefined, s"response cookies don't contains the biscuit attenuated token")

    val attenuatedToken = resp.json.at("cookies.biscuit-attenuated-token").get.asValue.at("value").get.asString
      .replace("Bearer ", "")
      .replace("Bearer: ", "")
      .replace("Bearer:", "")
      .replace("Biscuit ", "")
      .replace("Biscuit-Token ", "")
      .replace("Biscuit-Token", "")
      .replace("BiscuitToken ", "")
      .replace("BiscuitToken", "")
      .replace("biscuit: ", "")
      .replace("biscuit:", "")
      .replace("sealed-biscuit: ", "")
      .replace("sealed-biscuit:", "")
      .trim

    assert(attenuatedToken.nonEmpty, s"attenuated token is empty")

    val publicKeyFormatted = new PublicKey(demoKeyPair.getCurrentAlgo, publicKey)

    val encodedBiscuit = Biscuit.from_b64url(attenuatedToken, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().checks().size(), conf.checks.size, s"attenuated token doesn't contain checks list")

    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeId)
    client.forBiscuitEntity("biscuit-attenuators").deleteEntity(attenuator)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(demoKeyPair)
    await(2500.millis)
  }

  def testAttenuatorPluginQueryParams(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    val port = client.port

    // user("biscuit-studio-test");
    // role("user");
    val encodedToken = "EpYBCiwKE2Jpc2N1aXQtc3R1ZGlvLXRlc3QYAyIJCgcIChIDGIAIIggKBggGEgIYChIkCAASIIjCXLU5A-JBhCzBWklpKOr4azUhMLMXQzrhcLXTqxsfGkA6aVZxS-uu8tpQtbK_NxeaGxpc-WPBYPgO83NZQZSdLbE4ELcfgn-6OoH-jp6Ych7M_T3t1vBoNnSp4Paah9UHIiIKIBvwbLEZSZUtv2sQCY_UUBI-wBjBk9gnHXW4uQRGHzSv"

    val keypairId = "biscuit-keypair_a9aa8faf-eb60-46de-9644-ad1a71f1c160"
    val attenuatorId = "biscuit-attenuator_1d59b7f7-4245-4519-9743-e5d6cf10151f"
    val routeId = s"route_${UUID.randomUUID().toString}"
    val routeDomain = "attenuator-query-params.oto.tools"

    val publicKey = "0D6C7CBAECBA63916D7CAE5981C411EAF6A18929910709977FFDDFBD4433EF27"

    val demoKeyPair = BiscuitKeyPair(
      id = keypairId,
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      pubKey = publicKey,
      privKey = "1304480d1d9eea4075de296d8d84c2522703778c01b0a1459145716a7c33a665"
    )

    BiscuitKeyPairsUtils.createKeypairEntity(client)(demoKeyPair)

    val conf = AttenuatorConfig(
      checks = Seq("check if time($date), $date >= 2025-12-30T19:00:10Z;"),
    )

    val attenuator = BiscuitAttenuator(
      id = attenuatorId,
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypairId,
      config = conf
    )

    BiscuitAttenuatorsUtils.createAttenuatorEntity(client)(attenuator)

    val routeWithAttenuator = client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertRaw(routeId, Json.parse(
      s"""{
         |  "id": "${routeId}",
         |  "name": "biscuit-attenuator",
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
         |"plugins": [
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
         |      {
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
         |      "plugin": "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenAttenuator",
         |      "include": [],
         |      "exclude": [],
         |      "config": {
         |        "attenuator_ref": "${attenuatorId}",
         |        "extractor_type": "header",
         |        "extractor_name": "biscuit-token-test",
         |        "token_replace_loc": "query",
         |        "token_replace_name": "biscuit-attenuated-token"
         |      },
         |      "bound_listeners": [],
         |      "plugin_index": {
         |        "validate_access": 0
         |      }
         |    }
         |  ]
         |}""".stripMargin)).awaitf(awaitFor)
    assert(routeWithAttenuator.created, s"attenuator route has not been created")
    await(1300.millis)

    val headers = Map(
      "biscuit-token-test" -> encodedToken
    )

    val resp = client.call("GET", s"http://${routeDomain}:${port}", headers, None).awaitf(awaitFor)

    assertEquals(resp.status, 200, s"attenuator route did not respond with 200")
    assert(resp.json.at("query.biscuit-attenuated-token").isDefined, s"response headers don't contains the biscuit attenuated token")
    assert(resp.json.at("query.biscuit-attenuated-token").get.asString.nonEmpty, s"response headers don't contains the biscuit attenuated token")

    val attenuatedToken = resp.json.at("query.biscuit-attenuated-token").get.asString
      .replace("Bearer ", "")
      .replace("Bearer: ", "")
      .replace("Bearer:", "")
      .replace("Biscuit ", "")
      .replace("Biscuit-Token ", "")
      .replace("Biscuit-Token", "")
      .replace("BiscuitToken ", "")
      .replace("BiscuitToken", "")
      .replace("biscuit: ", "")
      .replace("biscuit:", "")
      .replace("sealed-biscuit: ", "")
      .replace("sealed-biscuit:", "")
      .trim

    assert(attenuatedToken.nonEmpty, s"attenuated token is empty")

    val publicKeyFormatted = new PublicKey(demoKeyPair.getCurrentAlgo, publicKey)

    val encodedBiscuit = Biscuit.from_b64url(attenuatedToken, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().checks().size(), conf.checks.size, s"attenuated token doesn't contain checks list")

    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeId)
    client.forBiscuitEntity("biscuit-attenuators").deleteEntity(attenuator)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(demoKeyPair)
    await(2500.millis)
  }

  def printHeader(str: String, what: String): Unit = {
    println("\n\n-----------------------------------------")
    println(s"  [${str}] - ${what}")
    println("-----------------------------------------\n\n")
  }

  override def beforeAll(): Unit = {
    otoroshi = startOtoroshiServer(port)
    client = clientFor(port)
    ec = otoroshi.executionContext
    mat = otoroshi.materializer
  }

  override def afterAll(): Unit = {
    otoroshi.stop()
  }

  test(s"create attenuator entity") {
    printHeader(attenuatorTest.name, "Create new attenuator entity")
    BiscuitAttenuatorsUtils.createAttenuatorEntity(client)(attenuatorTest)
  }

  test(s"attenuator plugin should throw an error - attenuator not provided") {
    printHeader("", "test verifier plugin without ref")
    testAttenuatorWithoutRef(client, 10.seconds)
  }

  test(s"testing attenuator in headers") {
    printHeader("", "testing attenuator plugin : result in headers")
    testAttenuatorPluginHeaders(client, 10.seconds)
  }

  test(s"testing attenuator in cookies") {
    printHeader("", "testing attenuator plugin : result in cookies")
    testAttenuatorPluginCookie(client, 10.seconds)
  }

  test(s"testing attenuator in query params") {
    printHeader("", "testing attenuator plugin : result in query params")
    testAttenuatorPluginQueryParams(client, 10.seconds)
  }
}