package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import org.biscuitsec.biscuit.crypto.KeyPair
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.models.EntityLocation
import otoroshi.next.models._
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenAttenuatorPlugin
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._

class TestAttenuators extends BiscuitStudioOneOtoroshiServerPerSuite {

  test("should be able to create an attenuator entity") {
    val attenuatorTest = BiscuitAttenuator(
      id = IdGenerator.namedId("biscuit-attenuator", otoroshi.env),
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = "",
      config = AttenuatorConfig(
        checks = Seq(
          "check if name(\"otoroshi-biscuit-studio-test\")",
          "check if user(\"biscuit-test-user\")",
          "check if role(\"user\")",
          "check if operation(\"read\")"
        )
      )
    )

    // Create the attenuator entity
    val attenuatorCreationRes = client.forBiscuitEntity("biscuit-attenuators").createEntity(attenuatorTest).awaitf(5.seconds)

    assert(attenuatorCreationRes.created, "attenuator entity should be created")
    val respAttenuatorChecks = attenuatorCreationRes.bodyJson.at("config.checks")

    assert(respAttenuatorChecks.isDefined, "created attenuator entity checks array should be defined")
    assert(respAttenuatorChecks.get.as[List[String]].nonEmpty, "created attenuator checks array should not be empty")
    assertEquals(respAttenuatorChecks.get.as[List[String]].size, 4, "checks array should contains 4 items")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forBiscuitEntity("biscuit-attenuators").deleteEntity(attenuatorTest).awaitf(5.seconds)
  }

  test("keypair reference not provided in attenuator entity") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val attenuatorTest = BiscuitAttenuator(
      id = IdGenerator.namedId("biscuit-attenuator", otoroshi.env),
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = "",
      config = AttenuatorConfig(
        checks = Seq(
          "check if name(\"otoroshi-biscuit-studio-test\")",
          "check if user(\"biscuit-test-user\")",
          "check if role(\"user\")",
          "check if operation(\"read\")"
        )
      )
    )

    val frontendDomain = "test-attenuator.oto.tools"

    val routeEndpoint = NgRoute(
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
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath(frontendDomain))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(NgPluginInstance(
        plugin = s"cp:${classOf[BiscuitTokenAttenuatorPlugin].getName}",
        config = NgPluginInstanceConfig(Json.obj(
          "attenuator_ref" -> attenuatorTest.id,
          "extractor_type" -> "header",
          "extractor_name" -> "Authorization",
          "token_replace_loc" -> "query",
          "token_replace_name" -> "auth"
        ))
      )))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuators").upsertEntity(attenuatorTest)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeEndpoint)

    await(3.seconds)

    val res0 = client.call("GET", s"http://${frontendDomain}:${port}", Map.empty, None).awaitf(5.seconds)

    assertEquals(res0.status, 500, s"verifier did not thrown an error 500")
    assert(res0.json.at("error").isDefined, s"error is not defined")
    assertEquals(res0.json.at("error").as[String], "keypair entity not found", s"bad error message for attenuator route")

    await(3.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuator").deleteEntity(attenuatorTest)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeEndpoint)
    await(5.seconds)
  }

  test("should not be able to add an attenuator plugin without attenuator entity reference") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
    )

    val attenuatorTest = BiscuitAttenuator(
      id = IdGenerator.namedId("biscuit-attenuator", otoroshi.env),
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = AttenuatorConfig(
        checks = Seq(
          "check if name(\"otoroshi-biscuit-studio-test\")",
          "check if user(\"biscuit-test-user\")",
          "check if role(\"user\")",
          "check if operation(\"read\")"
        )
      )
    )

    val frontendDomain = "test-attenuator.oto.tools"

    val routeEndpoint = NgRoute(
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
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath(frontendDomain))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(NgPluginInstance(
        plugin = s"cp:${classOf[BiscuitTokenAttenuatorPlugin].getName}",
        config = NgPluginInstanceConfig(Json.obj(
          "attenuator_ref" -> "",
          "extractor_type" -> "header",
          "extractor_name" -> "Authorization",
          "token_replace_loc" -> "query",
          "token_replace_name" -> "auth"
        ))
      )))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuators").upsertEntity(attenuatorTest)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeEndpoint)

    await(3.seconds)

    val res0 = client.call("GET", s"http://${frontendDomain}:${port}", Map.empty, None).awaitf(10.seconds)

    assertEquals(res0.status, 500, s"verifier did not thrown an error 500")
    assert(res0.json.at("error").isDefined, s"error is not defined")
    assertEquals(res0.json.at("error").as[String], "attenuator_ref not found in your plugin configuration", s"bad error message for attenuator route")

    await(3.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuator").deleteEntity(attenuatorTest)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeEndpoint)
    await(5.seconds)
  }

  test("should be able to attenuate a token with replace location to headers") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
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
          "role(\"user\");",
        ),
        resources = List.empty,
        rules = List.empty
      ),
      remoteFactsLoaderRef = None
    )

    val attenuatorTest = BiscuitAttenuator(
      id = IdGenerator.namedId("biscuit-attenuator", otoroshi.env),
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = AttenuatorConfig(
        checks = Seq(
          "check if resource(\"/folder1/file1.txt\")",
          "check if operation(\"read\")"
        )
      )
    )

    val frontendDomain = "test-attenuator.oto.tools"

    val routeEndpoint = NgRoute(
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
          plugin = s"cp:${classOf[BiscuitTokenAttenuatorPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "attenuator_ref" -> attenuatorTest.id,
            "extractor_type" -> "header",
            "extractor_name" -> "biscuit-token-test",
            "token_replace_loc" -> "header",
            "token_replace_name" -> "biscuit-attenuated-token"
          ))
        )
      ))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuators").upsertEntity(attenuatorTest)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeEndpoint)

    await(3.seconds)

    // Forge the token
    val respForgeToken = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${forge.id}/_generate",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), None).awaitf(5.seconds)

    assertEquals(respForgeToken.status, 200, s"verifier route did not respond with 200")
    assert(respForgeToken.json.at("token").isDefined, s"token not generated")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                   call the route with attenuator                               ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val headers = Map(
      "biscuit-token-test" -> respForgeToken.json.at("token").asString
    )

    val respAttenuatorRouteCall = client.call("GET", s"http://${frontendDomain}:${port}", headers, None).awaitf(5.seconds)
    assertEquals(respAttenuatorRouteCall.status, 200, s"attenuator route did not respond with 200")
    assert(respAttenuatorRouteCall.json.at("headers.biscuit-attenuated-token").isDefined, s"response cookies don't contains the biscuit attenuated token")
    assert(respAttenuatorRouteCall.json.at("headers.biscuit-attenuated-token").get.asString.nonEmpty, s"response cookies don't contains the biscuit attenuated token")

    val attenuatedToken = BiscuitExtractorConfig.replaceHeader(respAttenuatorRouteCall.json.at("headers.biscuit-attenuated-token").get.asString)
    assert(attenuatedToken.nonEmpty, s"attenuated token is empty")

    val encodedBiscuit = Biscuit.from_b64url(attenuatedToken, keypair.getPubKey)
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, forge.config.checks.size + attenuatorTest.config.checks.size, s"attenuated token doesn't contain checks list")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuator").deleteEntity(attenuatorTest)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").deleteEntity(forge)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeEndpoint)
    await(5.seconds)
  }

  test("should be able to attenuate a token with replace location to cookies") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
    )

    val forge = BiscuitTokenForge(
      id = IdGenerator.namedId("biscuit-forge", otoroshi.env),
      name = "New biscuit token",
      description = "New biscuit token",
      keypairRef = keypair.id,
      location = EntityLocation.default,
      config = BiscuitForgeConfig(
        checks = Seq(
          "check if user(\"biscuit-demo\")"
        ),
        facts = Seq(
          "user(\"biscuit-demo\");",
          "role(\"user\");",
        )
      )
    )

    val attenuatorTest = BiscuitAttenuator(
      id = IdGenerator.namedId("biscuit-attenuator", otoroshi.env),
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = AttenuatorConfig(
        checks = Seq(
          "check if resource(\"/folder1/file1.txt\")",
          "check if operation(\"read\")"
        )
      )
    )

    val frontendDomain = "test-attenuator.oto.tools"

    val routeEndpoint = NgRoute(
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
          plugin = s"cp:${classOf[BiscuitTokenAttenuatorPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "attenuator_ref" -> attenuatorTest.id,
            "extractor_type" -> "header",
            "extractor_name" -> "biscuit-token-test",
            "token_replace_loc" -> "cookie",
            "token_replace_name" -> "biscuit-attenuated-token"
          ))
        )
      ))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuators").upsertEntity(attenuatorTest)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeEndpoint)

    await(3.seconds)

    // Forge the token
    val respForgeToken = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${forge.id}/_generate",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), None).awaitf(5.seconds)

    assertEquals(respForgeToken.status, 200, s"verifier route did not respond with 200")
    assert(respForgeToken.json.at("token").isDefined, s"token not generated")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                   call the route with attenuator                               ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val headers = Map(
      "biscuit-token-test" -> respForgeToken.json.at("token").asString
    )

    val respAttenuatorRouteCall = client.call("GET", s"http://${frontendDomain}:${port}", headers, None).awaitf(5.seconds)
    assertEquals(respAttenuatorRouteCall.status, 200, s"attenuator route did not respond with 200")

    assert(respAttenuatorRouteCall.json.at("cookies.biscuit-attenuated-token.value").isDefined, s"response cookies don't contains the biscuit attenuated token")
    assert(respAttenuatorRouteCall.json.at("cookies.biscuit-attenuated-token.value").asString.nonEmpty, s"response cookies don't contains the biscuit attenuated token")

    val attenuatedToken = BiscuitExtractorConfig.replaceHeader(respAttenuatorRouteCall.json.at("cookies.biscuit-attenuated-token.value").asString)
    assert(attenuatedToken.nonEmpty, s"attenuated token is empty")

    val encodedBiscuit = Biscuit.from_b64url(attenuatedToken, keypair.getPubKey)
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, forge.config.checks.size + attenuatorTest.config.checks.size, s"attenuated token doesn't contain checks list")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuator").deleteEntity(attenuatorTest)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").deleteEntity(forge)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeEndpoint)
    await(5.seconds)
  }

  test("should be able to attenuate a token with replace location to query parameters") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
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
          "role(\"user\");",
        ),
        resources = List.empty,
        rules = List.empty
      ),
      remoteFactsLoaderRef = None
    )

    val attenuatorTest = BiscuitAttenuator(
      id = IdGenerator.namedId("biscuit-attenuator", otoroshi.env),
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = AttenuatorConfig(
        checks = Seq(
          "check if resource(\"/folder1/file1.txt\")",
          "check if operation(\"read\")"
        )
      )
    )

    val frontendDomain = "test-attenuator.oto.tools"

    val routeEndpoint = NgRoute(
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
          plugin = s"cp:${classOf[BiscuitTokenAttenuatorPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "attenuator_ref" -> attenuatorTest.id,
            "extractor_type" -> "header",
            "extractor_name" -> "biscuit-token-test",
            "token_replace_loc" -> "query",
            "token_replace_name" -> "biscuit-attenuated-token"
          ))
        )
      ))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuators").upsertEntity(attenuatorTest)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeEndpoint)

    await(3.seconds)

    // Forge the token
    val respForgeToken = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${forge.id}/_generate",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), None).awaitf(5.seconds)

    assertEquals(respForgeToken.status, 200, s"verifier route did not respond with 200")
    assert(respForgeToken.json.at("token").isDefined, s"token not generated")


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                   call the route with attenuator                               ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val headers = Map(
      "biscuit-token-test" -> respForgeToken.json.at("token").asString
    )

    val respAttenuatorRouteCall = client.call("GET", s"http://${frontendDomain}:${port}", headers, None).awaitf(5.seconds)
    assertEquals(respAttenuatorRouteCall.status, 200, s"attenuator route did not respond with 200")

    assert(respAttenuatorRouteCall.json.at("query.biscuit-attenuated-token").isDefined, s"response headers don't contains the biscuit attenuated token")
    assert(respAttenuatorRouteCall.json.at("query.biscuit-attenuated-token").asString.nonEmpty, s"response headers don't contains the biscuit attenuated token")

    val attenuatedToken = BiscuitExtractorConfig.replaceHeader(respAttenuatorRouteCall.json.at("query.biscuit-attenuated-token").asString)
    assert(attenuatedToken.nonEmpty, s"attenuated token is empty")

    val encodedBiscuit = Biscuit.from_b64url(attenuatedToken, keypair.getPubKey)
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, forge.config.checks.size + attenuatorTest.config.checks.size, s"attenuated token doesn't contain checks list")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuator").deleteEntity(attenuatorTest)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").deleteEntity(forge)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeEndpoint)
    await(5.seconds)
  }

  test("should be able to attenuate a token from query parameters and put it into cookies response") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
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
          "role(\"user\");",
        ),
        resources = List.empty,
        rules = List.empty
      ),
      remoteFactsLoaderRef = None
    )

    val attenuatorTest = BiscuitAttenuator(
      id = IdGenerator.namedId("biscuit-attenuator", otoroshi.env),
      name = "New Biscuit Attenuator entity",
      description = "New biscuit Attenuator entity",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = keypair.id,
      config = AttenuatorConfig(
        checks = Seq(
          "check if resource(\"/folder1/file1.txt\")",
          "check if operation(\"read\")"
        )
      )
    )

    val frontendDomain = "test-attenuator.oto.tools"

    val routeEndpoint = NgRoute(
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
          plugin = s"cp:${classOf[BiscuitTokenAttenuatorPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "attenuator_ref" -> attenuatorTest.id,
            "extractor_type" -> "query",
            "extractor_name" -> "biscuit-token",
            "token_replace_loc" -> "cookie",
            "token_replace_name" -> "biscuit-attenuated-token"
          ))
        )
      ))
    )

    // Create entities
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").upsertEntity(forge)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuators").upsertEntity(attenuatorTest)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeEndpoint)

    await(3.seconds)

    // Forge the token
    val respForgeToken = client.call("POST", s"http://otoroshi-api.oto.tools:${port}/api/extensions/biscuit/biscuit-forges/${forge.id}/_generate",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), None).awaitf(5.seconds)

    assertEquals(respForgeToken.status, 200, s"verifier route did not respond with 200")
    assert(respForgeToken.json.at("token").isDefined, s"token not generated")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                   call the route with attenuator                               ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val respAttenuatorRouteCall = client.call("GET", s"http://${frontendDomain}:${port}/?biscuit-token=${respForgeToken.json.at("token").asString}", Map.empty, None).awaitf(5.seconds)
    assertEquals(respAttenuatorRouteCall.status, 200, s"attenuator route did not respond with 200")

    assert(respAttenuatorRouteCall.json.at("cookies.biscuit-attenuated-token.value").isDefined, s"response cookies don't contains the biscuit attenuated token")
    assert(respAttenuatorRouteCall.json.at("cookies.biscuit-attenuated-token.value").asString.nonEmpty, s"response cookies don't contains the biscuit attenuated token")

    val attenuatedToken = BiscuitExtractorConfig.replaceHeader(respAttenuatorRouteCall.json.at("cookies.biscuit-attenuated-token.value").asString)
    assert(attenuatedToken.nonEmpty, s"attenuated token is empty")

    val encodedBiscuit = Biscuit.from_b64url(attenuatedToken, keypair.getPubKey)
    assertEquals(encodedBiscuit.authorizer().checks().asScala.flatMap(_._2.asScala).size, forge.config.checks.size + attenuatorTest.config.checks.size, s"attenuated token doesn't contain checks list")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-attenuator").deleteEntity(attenuatorTest)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-forges").deleteEntity(forge)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeEndpoint)
    await(5.seconds)
  }
}