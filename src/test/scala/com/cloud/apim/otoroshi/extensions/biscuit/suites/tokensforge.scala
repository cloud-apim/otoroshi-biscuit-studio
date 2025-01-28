package com.cloud.apim.otoroshi.extensions.biscuit.suites

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.domains.{BiscuitKeyPairsUtils, BiscuitTokensForgeUtils, BiscuitVerifiersUtils}
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitKeyPair, BiscuitRemoteFactsConfig, BiscuitTokenForge, BiscuitVerifier, RemoteFactsLoader, VerifierConfig}
import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitForgeConfig, BiscuitUtils}
import com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionSuite, OtoroshiClient}
import org.biscuitsec.biscuit.crypto.{KeyPair, PublicKey}
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.api.Otoroshi
import otoroshi.models.EntityLocation
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import play.api.libs.json.Json
import reactor.core.publisher.Mono

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class TestsTokensForge extends BiscuitExtensionSuite {
  val port: Int = freePort
  val entityId = s"biscuit-token_5042ae60-418d-4a92-9d50-57787b8970f5"
  val keypairID = s"biscuit-keypair_5a2ec0f9-35cd-48b2-9233-c024b5074df3"
  val tokenDemo = "EpYBCiwKDGJpc2N1aXQtZGVtbwoEMTIzNBgDIgkKBwgKEgMYgAgiCQoHCAYSAxiBCBIkCAASIB1ARKTqCEIC_2wnoCP78zyMUKhdWjEOHGOEHByZEUPkGkCAbpHaQKDBYKpc3GeiF40nZwsANGS-pSU3o9h-glQj3lF0Y1TtouAv6C7ur5siywAr-IR_1FMasRAPfAiXuZgDIiIKIOsz8F2Y43MgQVTVRJchzeDuZk8rRarjdTFLH_0dITub"
  implicit var ec: ExecutionContext = _
  implicit var mat: Materializer = _
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
    config = conf,
    remoteFactsLoaderRef = None
  )
  var otoroshi: Otoroshi = _
  var client: OtoroshiClient = _

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
      config = VerifierConfig(
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

  test(s"create a token forge with remote facts loader entity") {
    printHeader("", "create a token forge with remote facts loader entity")
    testForgeWithRemoteFactsEntity(client, 30.seconds)
  }

  test(s"create a token with forge from API") {
    printHeader("", "create a token with forge from API")
    testWithForgeFromApi(client, 30.seconds)
  }


  def testForgeWithRemoteFactsEntity(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  create API roles route                                        ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val (tport, _) = createTestServerWithRoutes("test", routes => routes.get("/api/roles", (req, response) => {
      response
        .status(200)
        .addHeader("Content-Type", "application/json")
        .sendString(Mono.just(
          s"""{
             |"acl": [
             |{
             |"user": "1234",
             |"resource": "resource1",
             |"action": "read"
             |},
             |{
             |"user": "1234",
             |"resource": "resource1",
             |"action": "write"
             |},
             |{
             |"user": "1234",
             |"resource": "resource2",
             |"action": "read"
             |}
             |]
             |}""".stripMargin))
    }))

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  create remote facts loader entity                             ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    val rfl = RemoteFactsLoader(
      id = IdGenerator.namedId("biscuit-remote-facts", otoroshi.env),
      name = "New biscuit remote facts loader",
      description = "New biscuit remote facts loader",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitRemoteFactsConfig(
        apiUrl = s"http://test.oto.tools:${tport}/api/roles",
        headers = Map(
          "Content-Type" -> "application/json"
        )
      )
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
        checks = Seq.empty,
        facts = Seq.empty,
        resources = Seq.empty,
        rules = Seq.empty
      ),
      remoteFactsLoaderRef = rfl.id.some
    )

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-remote-facts").upsertEntity(rfl)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "token-forges").upsertEntity(forge)
    await(2500.millis)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test API Roles route                                          ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val resp = client.call("GET", s"http://test.oto.tools:${tport}/api/roles", Map.empty, None).awaitf(awaitFor)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    assert(resp.json.at("acl").isDefined, s"acl array is not defined")

    val aclArr = resp.json.at("acl").as[List[Map[String, String]]]
    assertEquals(aclArr.length, 3, s"acl array length doesn't match")

    await(2500.millis)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test biscuit creation from forge                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val resp2 = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/_generate", Map("Content-Type" -> s"application/json"), Some(Json.obj(
      "config" -> forge.config.json,
      "keypair_ref" -> keypair.id,
      "remoteFactsLoaderRef" -> forge.remoteFactsLoaderRef.get,
    ))).awaitf(awaitFor)
    assertEquals(resp2.status, 200, s"verifier route did not respond with 200")
    assert(resp2.json.at("done").isDefined, s"acl array is not defined")
    assert(resp2.json.at("done").asBoolean, s"acl array is not defined")
    assert(resp2.json.at("token").isDefined, s"acl array is not defined")

    val token = BiscuitUtils.replaceHeader(resp2.json.at("token").get.asString)
    assert(token.nonEmpty, s"token is empty")

    val publicKeyFormatted = new PublicKey(biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519, keypair.pubKey)

    val encodedBiscuit = Biscuit.from_b64url(token, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().facts().size(), aclArr.length, s"token doesn't contain all remote facts")

    await(2500.millis)
  }


  def testWithForgeFromApi(client: OtoroshiClient, awaitFor: FiniteDuration)(implicit ec: ExecutionContext, mat: Materializer): Unit = {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup forge                                                   ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        checks = Seq(
          "check if user(\"biscuit-demo\")"
        ),
        facts = Seq(
          "user(\"biscuit-demo\");",
          "role(\"1234\");",
        ),
        resources = List.empty,
        rules = List.empty
      ),
      remoteFactsLoaderRef = None
    )

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "token-forges").upsertEntity(forge)
    await(2500.millis)


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test API                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val resp = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/token-forges/${forge.id}/_generate",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), None).awaitf(awaitFor)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    assert(resp.json.at("token").isDefined, s"token not generated")

    val token = BiscuitUtils.replaceHeader(resp.json.at("token").get.asString)
    assert(token.nonEmpty, s"token is empty")

    val publicKeyFormatted = new PublicKey(biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519, keypair.pubKey)

    val encodedBiscuit = Biscuit.from_b64url(token, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().facts().size(), forge.config.facts.length, s"token doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().size(), forge.config.checks.length, s"token doesn't contain all checks")

    await(2500.millis)
  }

}
