package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.domains.BiscuitVerifiersUtils
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import org.biscuitsec.biscuit.crypto.KeyPair
import org.biscuitsec.biscuit.token.Biscuit
import org.joda.time.DateTime
import otoroshi.models.EntityLocation
import otoroshi.next.models._
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenAttenuatorPlugin
import play.api.libs.json.Json
import reactor.core.publisher.Mono

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._

class TestRemoteFactsEntity extends BiscuitStudioOneOtoroshiServerPerSuite {

  test("should be able to connect to remote facts API and verify fields") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  create the API                                                ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val (tport1, _) = createTestServerWithRoutes("test-api", routes => routes.post("/api/facts", (req, response) => {
      req.receive().retain().asString().flatMap { body =>
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
               |],
               |"facts": [
               |{
               |  "name": "role",
               |  "value": "dev"
               |},
               |{
               |  "name": "time",
               |  "value": "${DateTime.now}"
               |},
               |{
               |  "name": "server",
               |  "value": "${UUID.randomUUID()}"
               |},
               |{
               |  "name": "version",
               |  "value": "dev"
               |}
               |]
               |}""".stripMargin))
      }
    }))

    await(2500.millis)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test API Roles route                                          ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val resp = client.call("POST", s"http://test-api.oto.tools:${tport1}/api/facts", Map("Content-Type" -> "application/json"), Some(Json.obj("foo" -> "bar"))).awaitf(5.seconds)
    assertEquals(resp.status, 200, s"remote facts API did not respond with 200")
    assert(resp.json.at("acl").isDefined, s"acl array is not defined")
    assert(resp.json.at("facts").isDefined, s"facts array is not defined")

    val aclArr = resp.json.at("acl").as[List[Map[String, String]]]
    assertEquals(aclArr.length, 3, s"acl array length doesn't match")

    val factsArr = resp.json.at("facts").as[List[Map[String, String]]]
    assertEquals(factsArr.length, 4, s"facts array length doesn't match")

    await(2500.millis)
  }

  test("should be able to forge and VERIFY a token with remote facts from entity") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  create the 'Remote Facts' API for attenuator                  ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val domainAPIPrefix = "test-api-verifier"
    val routeAPIPath = "/api/checks"

    val (tport, _) = createTestServerWithRoutes(domainAPIPrefix, routes => routes.post(routeAPIPath, (req, response) => {
      req.receive().retain().asString().flatMap { body =>
        response
          .status(200)
          .addHeader("Content-Type", "application/json")
          .sendString(Mono.just(
            s"""{
               |"checks": [
               |  "check if role(\\"dev\\")",
               |  "check if user(\\"biscuit-demo\\")",
               |  "check if version(\\"dev\\")",
               |  "check if name(\\"otoroshi-biscuit-studio-test\\")"
               |]
               |}""".stripMargin))
      }
    }))

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  create the 'Remote Facts' API for forge                       ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val domainAPIForgePrefix = "test-api-forge"
    val routeAPIForgePath = "/api/facts"
    val (forgeApiPort, _) = createTestServerWithRoutes(domainAPIForgePrefix, routes => routes.post(routeAPIForgePath, (req, response) => {
      req.receive().retain().asString().flatMap { body =>
        response
          .status(200)
          .addHeader("Content-Type", "application/json")
          .sendString(Mono.just(
            s"""{
               |"facts": [
               |{
               |  "name": "role",
               |  "value": "dev"
               |},
               |{
               |  "name": "user",
               |  "value": "biscuit-demo"
               |},
               |{
               |  "name": "version",
               |  "value": "dev"
               |}
               |]
               |}""".stripMargin))
      }
    }))

    val fullDomain = s"http://${domainAPIPrefix}.oto.tools:${tport}${routeAPIPath}"
    val fullDomainForForge = s"http://${domainAPIForgePrefix}.oto.tools:${forgeApiPort}${routeAPIForgePath}"

    val rflForVerifier = RemoteFactsLoader(
      id = IdGenerator.namedId("biscuit-remote-facts", otoroshi.env),
      name = "New biscuit remote facts loader",
      description = "New biscuit remote facts loader",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitRemoteFactsConfig(
        apiUrl = fullDomain,
        headers = Map(
          "Content-Type" -> "application/json"
        )
      )
    )

    val rflForForge = RemoteFactsLoader(
      id = IdGenerator.namedId("biscuit-remote-facts", otoroshi.env),
      name = "New biscuit remote facts loader for forge",
      description = "New biscuit remote facts loader for forge",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitRemoteFactsConfig(
        apiUrl = fullDomainForForge,
        headers = Map(
          "Content-Type" -> "application/json"
        )
      )
    )

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

    val forge = BiscuitTokenForge(
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
        ),
      ),
      remoteFactsLoaderRef = rflForForge.id.some
    )

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-remote-facts").upsertEntity(rflForVerifier)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-remote-facts").upsertEntity(rflForForge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test remote facts API for forge                               ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respRemoteAPIForge = client.call("POST", fullDomainForForge, Map("Content-Type" -> "application/json"), Some(Json.obj("foo" -> "bar"))).awaitf(5.seconds)
    assertEquals(respRemoteAPIForge.status, 200, s"FORGE remote facts API did not respond with 200")
    assert(respRemoteAPIForge.json.at("facts").isDefined, s"forge facts array is not defined")

    val forgeFactsArr = respRemoteAPIForge.json.at("facts").as[List[Map[String, String]]]
    assertEquals(forgeFactsArr.length, 3, s"forge facts array length doesn't match")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test biscuit creation from forge                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val resp2 = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/_generate", Map("Content-Type" -> s"application/json"), Some(Json.obj(
      "config" -> forge.config.json,
      "keypair_ref" -> keypair.id,
      "remoteFactsLoaderRef" -> forge.remoteFactsLoaderRef.get,
    ))).awaitf(5.seconds)
    assertEquals(resp2.status, 200, s"verifier route did not respond with 200")
    assert(resp2.json.at("done").isDefined, s"generation of the token failed")
    assert(resp2.json.at("done").asBoolean, s"token has not been well generated")
    assert(resp2.json.at("token").isDefined, s"token has not been generated")

    val token = BiscuitExtractorConfig.replaceHeader(resp2.json.at("token").get.asString)
    assert(token.nonEmpty, s"token is empty")

    val encodedBiscuit = Biscuit.from_b64url(token, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().facts().size(), forge.config.facts.length + forge.config.resources.length + forgeFactsArr.length, s"token doesn't contain all facts (from forge + remote facts)")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                      create the verifier entity and the route with verifier                  ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val verifierId = s"biscuit-verifier_${UUID.randomUUID()}"

    val verifier = BiscuitVerifier(
      id = verifierId,
      name = "New Biscuit Verifier entity",
      description = "New biscuit Verifier entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = VerifierConfig(
        checks = List.empty,
        facts = List.empty,
        resources = List.empty,
        rules = List.empty,
        policies = List.empty,
        revokedIds = List.empty,
        remoteFactsRefs = Seq(
          s"${rflForVerifier.id}"
        )
      ),
      extractor = BiscuitExtractorConfig(
        "header",
        "biscuit-header"
      )
    )

    BiscuitVerifiersUtils.createVerifierEntity(client)(verifier)

    val routeId = s"route_${UUID.randomUUID().toString}"
    val routeDomain = "verifier-headers.oto.tools"

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
         |        "verifier_refs": ["${verifierId}"],
         |        "enforce": true
         |      },
         |      "bound_listeners": [],
         |      "plugin_index": {
         |        "validate_access": 0
         |      }
         |    }
         |  ]
         |}""".stripMargin)).awaitf(5.seconds)
    assert(routeWithAttenuator.created, s"attenuator route has not been created")
    await(1500.millis)


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test remote facts API for verifier                            ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respVerifierAPI = client.call("POST", fullDomain, Map("Content-Type" -> "application/json"), Some(Json.obj("foo" -> "bar"))).awaitf(5.seconds)

    assertEquals(respVerifierAPI.status, 200, s"verifier remote facts API did not respond with 200")
    assert(respVerifierAPI.json.at("checks").isDefined, s"verifier facts array is not defined")

    val respVerifierAPIChecks = respVerifierAPI.json.at("checks").as[List[String]]

    assertEquals(respVerifierAPIChecks.length, 4, s"verifier checks from API array length doesn't match")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  call the verifier route with the forged token                 ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respWithBadTokenHeader = client.call("GET", s"http://${routeDomain}:${port}", Map("biscuit-test" -> token), None).awaitf(5.seconds)

    assertEquals(respWithBadTokenHeader.status, 403, s"route should return forbidden for bad token in headers")
    assert(respWithBadTokenHeader.json.at("Otoroshi-Error").isDefined, s"'Otoroshi-Error' should be defined")

    val respWithGoodTokenHeader = client.call("GET", s"http://${routeDomain}:${port}", Map("biscuit-header" -> token), None).awaitf(5.seconds)

    assertEquals(respWithGoodTokenHeader.status, 200, s"verifier route with good token in headers did not respond with 200")

    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteRaw(routeId)
    client.forBiscuitEntity("biscuit-verifiers").deleteEntity(verifier)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(keypair)
    await(2500.millis)
  }


  test("should be able to forge and attenuate a token with remote facts from entity") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  create the 'Remote Facts' API for attenuator                  ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val domainAPIPrefix = "test-api-attenuator"
    val routeAPIPath = "/api/checks"

    val (tport, _) = createTestServerWithRoutes(domainAPIPrefix, routes => routes.post(routeAPIPath, (req, response) => {
      req.receive().retain().asString().flatMap { body =>
        response
          .status(200)
          .addHeader("Content-Type", "application/json")
          .sendString(Mono.just(
            s"""{
               |"checks": [
               |  "check if resource(\\"file1\\")",
               |  "check if role(\\"admin\\")",
               |  "check if operation(\\"read\\")"
               |]
               |}""".stripMargin))
      }
    }))

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  create the 'Remote Facts' API for forge                       ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val domainAPIForgePrefix = "test-api-forge"
    val routeAPIForgePath = "/api/facts"
    val (forgeApiPort, _) = createTestServerWithRoutes(domainAPIForgePrefix, routes => routes.post(routeAPIForgePath, (req, response) => {
      req.receive().retain().asString().flatMap { body =>
        response
          .status(200)
          .addHeader("Content-Type", "application/json")
          .sendString(Mono.just(
            s"""{
               |"facts": [
               |{
               |  "name": "role",
               |  "value": "dev"
               |},
               |{
               |  "name": "user",
               |  "value": "biscuit-demo"
               |},
               |{
               |  "name": "version",
               |  "value": "dev"
               |}
               |]
               |}""".stripMargin))
      }
    }))

    val fullDomain = s"http://${domainAPIPrefix}.oto.tools:${tport}${routeAPIPath}"
    val fullDomainForForge = s"http://${domainAPIForgePrefix}.oto.tools:${forgeApiPort}${routeAPIForgePath}"

    val rfl = RemoteFactsLoader(
      id = IdGenerator.namedId("biscuit-remote-facts", otoroshi.env),
      name = "New biscuit remote facts loader",
      description = "New biscuit remote facts loader",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitRemoteFactsConfig(
        apiUrl = fullDomain,
        headers = Map(
          "Content-Type" -> "application/json"
        )
      )
    )

    val rflForForge = RemoteFactsLoader(
      id = IdGenerator.namedId("biscuit-remote-facts", otoroshi.env),
      name = "New biscuit remote facts loader for forge",
      description = "New biscuit remote facts loader for forge",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitRemoteFactsConfig(
        apiUrl = fullDomainForForge,
        headers = Map(
          "Content-Type" -> "application/json"
        )
      )
    )

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

    val forge = BiscuitTokenForge(
      id = IdGenerator.namedId("biscuit-forge", otoroshi.env),
      name = "New biscuit token",
      description = "New biscuit token",
      keypairRef = keypair.id,
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = BiscuitForgeConfig(
        facts = Seq(
          "user(\"demo\")"
        ),
      ),
      remoteFactsLoaderRef = rflForForge.id.some
    )

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-remote-facts").upsertEntity(rfl)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-remote-facts").upsertEntity(rflForForge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test remote facts API for forge                               ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respRemoteAPIForge = client.call("POST", fullDomainForForge, Map("Content-Type" -> "application/json"), Some(Json.obj("foo" -> "bar"))).awaitf(5.seconds)
    assertEquals(respRemoteAPIForge.status, 200, s"FORGE remote facts API did not respond with 200")
    assert(respRemoteAPIForge.json.at("facts").isDefined, s"forge facts array is not defined")

    val remoteForgeFacts = respRemoteAPIForge.json.at("facts").as[List[Map[String, String]]]
    assertEquals(remoteForgeFacts.length, 3, s"forge facts array length doesn't match")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test biscuit creation from forge                              ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val resp2 = client.call("POST", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/tokens/_generate", Map("Content-Type" -> s"application/json"), Some(Json.obj(
      "config" -> forge.config.json,
      "keypair_ref" -> keypair.id,
      "remoteFactsLoaderRef" -> forge.remoteFactsLoaderRef.get,
    ))).awaitf(5.seconds)
    assertEquals(resp2.status, 200, s"verifier route did not respond with 200")
    assert(resp2.json.at("done").isDefined, s"generation of the token failed")
    assert(resp2.json.at("done").asBoolean, s"token has not been well generated")
    assert(resp2.json.at("token").isDefined, s"token has not been generated")

    val token = BiscuitExtractorConfig.replaceHeader(resp2.json.at("token").get.asString)
    assert(token.nonEmpty, s"token is empty")

    val encodedBiscuit = Biscuit.from_b64url(token, publicKeyFormatted)
    assertEquals(encodedBiscuit.authorizer().facts().size(), remoteForgeFacts.length + forge.config.facts.length, s"token doesn't contain all remote facts")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                      create the attenuator entity and the route with attenuator                ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
          "check if user(\"biscuit-demo\")"
        )
      )
    )

    val attenuatorRouteDomain = "attenuator-headers.oto.tools"

    val routeWithAttenuator = NgRoute(
      location = EntityLocation.default,
      id = UUID.randomUUID().toString,
      name = "test attenuator route",
      description = "test attenuator route",
      tags = Seq.empty,
      metadata = Map.empty,
      enabled = true,
      debugFlow = false,
      capture = false,
      exportReporting = false,
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath(attenuatorRouteDomain))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(
        NgPluginInstance(
          plugin = s"cp:otoroshi.next.plugins.EchoBackend",
          config = NgPluginInstanceConfig(Json.obj(
            "limit" -> "524288"
          ))
        ),
        NgPluginInstance(
          plugin = s"cp:${classOf[BiscuitTokenAttenuatorPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "attenuator_ref" -> attenuator.id,
            "extractor_type" -> "header",
            "extractor_name" -> "biscuit-token-test",
            "token_replace_loc" -> "header",
            "token_replace_name" -> "biscuit-attenuated-token",
            "remote_facts_ref" -> rfl.id,
            "enable_remote_facts" -> true
          ))
        )))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuators").upsertEntity(attenuator)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeWithAttenuator)

    await(3.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test remote facts API for attenuator                               ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respAttenuatorAPI = client.call("POST", fullDomain, Map("Content-Type" -> "application/json"), Some(Json.obj("foo" -> "bar"))).awaitf(5.seconds)

    assertEquals(respAttenuatorAPI.status, 200, s"attenuator remote facts API did not respond with 200")
    assert(respAttenuatorAPI.json.at("checks").isDefined, s"attenuator facts array is not defined")

    val attenuatorAPIChecks = respAttenuatorAPI.json.at("checks").as[List[String]]
    assertEquals(attenuatorAPIChecks.length, 3, s"attenuator checks from API array length doesn't match")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  call the attenuator route with the forged token               ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val headers = Map(
      "biscuit-token-test" -> token
    )

    val resp3 = client.call("GET", s"http://${attenuatorRouteDomain}:${port}", headers, None).awaitf(5.seconds)
    assertEquals(resp3.status, 200, s"attenuator route did not respond with 200")
    assert(resp3.json.at("headers.biscuit-attenuated-token").isDefined, s"response headers don't contains the biscuit attenuated token")

    val attenuatedTokenStr = BiscuitExtractorConfig.replaceHeader(resp3.json.at("headers.biscuit-attenuated-token").asString)
    assert(attenuatedTokenStr.nonEmpty, s"attenuated token is empty")

    val attenuatedToken = Biscuit.from_b64url(attenuatedTokenStr, publicKeyFormatted)

    println(s"got ATTENUATED token = ${attenuatedTokenStr}")
    assertEquals(attenuatedToken.authorizer().checks().asScala.flatMap(_._2.asScala).size, attenuator.config.checks.size + attenuatorAPIChecks.length, s"attenuated token doesn't contain checks list")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeWithAttenuator)
    client.forBiscuitEntity("biscuit-attenuators").deleteEntity(attenuator)
    client.forBiscuitEntity("biscuit-keypairs").deleteEntity(keypair)
    await(2.seconds)
  }

}
