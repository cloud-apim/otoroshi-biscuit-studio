package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import otoroshi.models.EntityLocation
import otoroshi.next.models._
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitRemoteTokenFetcherPlugin
import play.api.libs.json.Json
import reactor.core.publisher.Mono

import java.util.UUID
import scala.concurrent.duration.DurationInt

class RemoteTokenFetcherSuite extends BiscuitStudioOneOtoroshiServerPerSuite {

  test("should be able to fetch a remote token from API (and insert it into headers)") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val (tokenApiPort, _) = createTestServerWithRoutes("test", routes => routes.get("/api/token", (req, response) => {
      response
        .status(200)
        .sendString(Mono.just(
          "En0KEwoEMTIzNBgDIgkKBwgKEgMYgAgSJAgAEiAs2CFWr5WyHHWEiMhTXxVNw4gP7PlADPaGfr_AQk9WohpA6LZTjFfFhcFQrMsp2O7bOI9BOzP-jIE5PGhha62HDfX4t5FLQivX5rUhH5iTv2c-rd0kDSazrww4cD1UCeytDSIiCiCfMgpVPOuqq371l1wHVhCXoIscKW-wrwiKN80vR_Rfzg=="
        ))
    }))

    val frontendDomain = "test.oto.tools"

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
          plugin = s"cp:${classOf[BiscuitRemoteTokenFetcherPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "api_url" -> s"http://localhost:${tokenApiPort}/api/token",
            "api_method" -> "GET",
            "token_replace_loc" -> "header",
            "token_replace_name" -> "biscuit-fetched"
          )))
      ))
    )

    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeApi)
    await(2.seconds)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test                                                          ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val res = client.call("GET", s"http://${frontendDomain}:${port}", Map.empty, None).awaitf(10.seconds)
    assertEquals(res.status, 200, "status should be 200")

    assert(res.json.at("headers.biscuit-fetched").isDefined, s"response cookies don't contains the biscuit attenuated token")
    assert(res.json.at("headers.biscuit-fetched").asString.nonEmpty, s"response cookies don't contains the biscuit attenuated token")

    val attenuatedToken = BiscuitExtractorConfig.replaceHeader(res.json.at("headers.biscuit-fetched").asString)
    assert(attenuatedToken.nonEmpty, s"attenuated token is empty")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeApi)
    await(2.seconds)
  }

  test("should be able to fetch a remote token from API (and insert it into cookies)") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val (tokenApiPort, _) = createTestServerWithRoutes("test", routes => routes.get("/api/token", (req, response) => {
      response
        .status(200)
        .addHeader("Content-Type", "application/json")
        .sendString(
          Mono.just(
            s"""{
               |  "token": "En0KEwoEMTIzNBgDIgkKBwgKEgMYgAgSJAgAEiAs2CFWr5WyHHWEiMhTXxVNw4gP7PlADPaGfr_AQk9WohpA6LZTjFfFhcFQrMsp2O7bOI9BOzP-jIE5PGhha62HDfX4t5FLQivX5rUhH5iTv2c-rd0kDSazrww4cD1UCeytDSIiCiCfMgpVPOuqq371l1wHVhCXoIscKW-wrwiKN80vR_Rfzg=="
               |}""".stripMargin
          )
        )
    }))

    val frontendDomain = "test.oto.tools"

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
          plugin = s"cp:${classOf[BiscuitRemoteTokenFetcherPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "api_url" -> s"http://localhost:${tokenApiPort}/api/token",
            "api_method" -> "GET",
            "token_replace_loc" -> "cookie",
            "token_replace_name" -> "biscuit-fetched"
          )))
      ))
    )

    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeApi)
    await(2.seconds)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test                                                          ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val res = client.call("GET", s"http://${frontendDomain}:${port}", Map.empty, None).awaitf(10.seconds)
    assertEquals(res.status, 200, "status should be 200")

    assert(res.json.at("cookies.biscuit-fetched.value").isDefined, s"response cookies don't contains the biscuit attenuated token")
    assert(res.json.at("cookies.biscuit-fetched.value").asString.nonEmpty, s"response cookies don't contains the biscuit attenuated token")

    val attenuatedToken = BiscuitExtractorConfig.replaceHeader(res.json.at("cookies.biscuit-fetched.value").asString)
    assert(attenuatedToken.nonEmpty, s"attenuated token is empty")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeApi)
    await(2.seconds)
  }

  test("should be able to fetch a remote token from API (and insert it into query parameters)") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val (tokenApiPort, _) = createTestServerWithRoutes("test", routes => routes.get("/api/token", (req, response) => {
      response
        .status(200)
        .sendString(Mono.just(
          "En0KEwoEMTIzNBgDIgkKBwgKEgMYgAgSJAgAEiAs2CFWr5WyHHWEiMhTXxVNw4gP7PlADPaGfr_AQk9WohpA6LZTjFfFhcFQrMsp2O7bOI9BOzP-jIE5PGhha62HDfX4t5FLQivX5rUhH5iTv2c-rd0kDSazrww4cD1UCeytDSIiCiCfMgpVPOuqq371l1wHVhCXoIscKW-wrwiKN80vR_Rfzg=="
        ))
    }))

    val frontendDomain = "test.oto.tools"

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
          plugin = s"cp:${classOf[BiscuitRemoteTokenFetcherPlugin].getName}",
          config = NgPluginInstanceConfig(Json.obj(
            "api_url" -> s"http://localhost:${tokenApiPort}/api/token",
            "api_method" -> "GET",
            "token_replace_loc" -> "query",
            "token_replace_name" -> "biscuit-fetched"
          )))
      ))
    )

    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeApi)
    await(2.seconds)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  test                                                          ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val res = client.call("GET", s"http://${frontendDomain}:${port}", Map.empty, None).awaitf(10.seconds)
    assertEquals(res.status, 200, "status should be 200")

    assert(res.json.at("query.biscuit-fetched").isDefined, s"response cookies don't contains the biscuit attenuated token")
    assert(res.json.at("query.biscuit-fetched").asString.nonEmpty, s"response cookies don't contains the biscuit attenuated token")

    val attenuatedToken = BiscuitExtractorConfig.replaceHeader(res.json.at("query.biscuit-fetched").asString)
    assert(attenuatedToken.nonEmpty, s"attenuated token is empty")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeApi)
    await(2.seconds)
  }

}
