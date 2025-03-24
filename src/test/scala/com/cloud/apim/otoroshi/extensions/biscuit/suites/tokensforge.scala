package com.cloud.apim.otoroshi.extensions.biscuit.suites

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.domains.BiscuitVerifiersUtils
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionSuite, OtoroshiClient}
import org.biscuitsec.biscuit.crypto.{KeyPair, PublicKey}
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.api.Otoroshi
import otoroshi.models.EntityLocation
import otoroshi.next.models._
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenVerifierPlugin
import play.api.libs.json.Json
import reactor.core.publisher.Mono

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.CollectionConverters._

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
      ),
      extractor = BiscuitExtractorConfig(
        "header", "biscuit-header"
      )
    )

    BiscuitVerifiersUtils.createVerifierEntity(client)(verifier)

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

    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeWithVerifier)
    await(3.seconds)

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
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(demoKeyPair).awaitf(5.seconds)

    printHeader(biscuitToken.name, "Create biscuit token entity")

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(biscuitToken).awaitf(5.seconds)

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
    val (tport, _) = createTestServerWithRoutes("test", routes => routes.post("/api/roles", (req, response) => {
      req.receive().retain().asString().flatMap { body =>
        println(body)
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
      }
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
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(2500.millis)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test API Roles route                                          ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val resp = client.call("POST", s"http://test.oto.tools:${tport}/api/roles", Map("Content-Type" -> "application/json"), Some(Json.obj("foo" -> "bar"))).awaitf(awaitFor)
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

    val token = BiscuitExtractorConfig.replaceHeader(resp2.json.at("token").get.asString)
    assert(token.nonEmpty, s"token is empty")

    val publicKeyFormatted = new PublicKey(keypair.getCurrentAlgo, keypair.pubKey)

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
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(2500.millis)


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test API                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val resp = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${forge.id}/_generate",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), None).awaitf(awaitFor)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    assert(resp.json.at("token").isDefined, s"token not generated")

    val token = BiscuitExtractorConfig.replaceHeader(resp.json.at("token").get.asString)
    assert(token.nonEmpty, s"token is empty")

    val publicKeyFormatted = new PublicKey(keypair.getCurrentAlgo, keypair.pubKey)

    val encodedBiscuit = Biscuit.from_b64url(token, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().facts().size(), forge.config.facts.length, s"token doesn't contain all facts")
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, forge.config.checks.length, s"token doesn't contain all checks")

    await(2500.millis)
  }

}
