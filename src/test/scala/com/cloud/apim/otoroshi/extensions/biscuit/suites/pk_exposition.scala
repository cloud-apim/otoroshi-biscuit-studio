package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitKeyPair
import org.biscuitsec.biscuit.crypto.KeyPair
import otoroshi.models.EntityLocation
import otoroshi.next.models._
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.{BiscuitExposePubKeysPluginConfig, ExposeBiscuitPublicKeysPlugin}
import play.api.libs.json.{JsObject, Json}

import java.util.UUID
import scala.concurrent.duration.DurationInt

class TestBiscuitExpositionKpPlugin extends BiscuitStudioOneOtoroshiServerPerSuite {

  test("should be able to expose public keypairs without specifying a list of authorized keys") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val biscuitKeyPair = new KeyPair()
    val biscuitKeyPair2 = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
      isPublic = true
    )

    val keypair2 = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "Test Keypair 2",
      description = "Test Keypair 2",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = biscuitKeyPair2.toHex,
      pubKey = biscuitKeyPair2.public_key().toHex,
    )

    val routeEndpoint = NgRoute(
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
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath("test-keypairs.oto.tools/.well-known/biscuit-web-keys"))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(NgPluginInstance(
        plugin = s"cp:${classOf[ExposeBiscuitPublicKeysPlugin].getName}",
        config = NgPluginInstanceConfig(BiscuitExposePubKeysPluginConfig().json.asObject)
      )))
    )

    println(s"got config = ${routeEndpoint.plugins.json}")

    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair2)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeEndpoint)

    await(5.seconds)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                  test   1 - should get only 2 keys : key 1 and key 2                           ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val res0 = client.call("GET", s"http://test-keypairs.oto.tools:${port}/.well-known/biscuit-web-keys", Map.empty, None).awaitf(30.seconds)
    assertEquals(res0.status, 200, "status should be 200")
    assert(res0.json.select("items").asOpt[List[JsObject]].nonEmpty, "array of keypairs should not be empty")
    assertEquals(res0.json.select("items").asOpt[List[JsObject]].get.size, 1, "array should contains 1 value")
    assert(res0.json.at("items").asOpt[List[JsObject]].get.head.at("key_bytes").isDefined, "key_bytes should be defined")
    assertEquals(res0.json.at("items").asOpt[List[JsObject]].get.head.at("key_bytes").get.asString, keypair.pubKey, "public keypair key_bytes should be equals to pubkey")

    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair2)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeEndpoint)
    await(5.seconds)
  }


  test("should be able to expose public keypairs with specifying keys in plugin configuration") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  setup                                                         ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val biscuitKeyPair3 = new KeyPair()
    val biscuitKeyPair4 = new KeyPair()
    val biscuitKeyPair5 = new KeyPair()

    val keypair3 = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "Test Keypair 3",
      description = "Test Keypair 3",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = biscuitKeyPair3.toHex,
      pubKey = biscuitKeyPair3.public_key().toHex
    )

    val keypair4 = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "Test Keypair 4",
      description = "Test Keypair 4",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = biscuitKeyPair4.toHex,
      pubKey = biscuitKeyPair4.public_key().toHex,
      isPublic = true
    )

    val keypair5 = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "Test Keypair 5",
      description = "Test Keypair 5",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = biscuitKeyPair5.toHex,
      pubKey = biscuitKeyPair5.public_key().toHex,
    )

    val routeEndpoint2 = NgRoute(
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
      frontend = NgFrontend.empty.copy(domains = Seq(NgDomainAndPath("test-keypairs2.oto.tools/.well-known/biscuit-web-keys"))),
      backend = NgBackend.empty.copy(targets = Seq(NgTarget.default)),
      plugins = NgPlugins(Seq(NgPluginInstance(
        plugin = s"cp:${classOf[ExposeBiscuitPublicKeysPlugin].getName}",
        config = NgPluginInstanceConfig(Json.obj(
          "authorized_pk_list" -> Seq(
            keypair3.id,
            keypair5.id
          ),
        ))
      )))
    )


    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair3)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair4)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").upsertEntity(keypair5)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").upsertEntity(routeEndpoint2)

    await(5.seconds)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                  test   2 - should get only 2 keys : key 3 and key 4                           ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val res0 = client.call("GET", s"http://test-keypairs2.oto.tools:${port}/.well-known/biscuit-web-keys", Map.empty, None).awaitf(30.seconds)
    assertEquals(res0.status, 200, "status should be 200")



    assertEquals(res0.status, 200, "status should be 200")
    assert(res0.json.select("items").asOpt[List[JsObject]].nonEmpty, "array of keypairs should not be empty")
    assertEquals(res0.json.select("items").asOpt[List[JsObject]].get.size, 2, "array should contains 2 values")

    // check the first key
    val firstKey = res0.json.at("items").asOpt[List[JsObject]].get.find(kp => kp.at("key_id").isDefined && kp.at("key_id").asString.equals(keypair3.id))
    val secondKey = res0.json.at("items").asOpt[List[JsObject]].get.find(kp => kp.at("key_id").isDefined && kp.at("key_id").asString.equals(keypair5.id))

    assert(firstKey.isDefined, "first key should be defined")
    assert(firstKey.get.at("key_bytes").isDefined, "first key - key_bytes should be defined")
    assertEquals(firstKey.get.at("key_bytes").get.asString, keypair3.pubKey, "first key - public keypair key_bytes should be equals to pubkey")

    // check the second key
    assert(secondKey.isDefined, "second key should be defined")
    assert(secondKey.get.at("key_bytes").isDefined, "second key - key_bytes should be defined")
    assertEquals(secondKey.get.at("key_bytes").get.asString, keypair5.pubKey, "second key - public keypair key_bytes should be equals to pubkey")

    await(5.seconds)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                  teardown                                                      ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair3)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair4)
    client.forEntity("biscuit.extensions.cloud-apim.com", "v1", "biscuit-keypairs").deleteEntity(keypair5)
    client.forEntity("proxy.otoroshi.io", "v1", "routes").deleteEntity(routeEndpoint2)
    await(5.seconds)
  }

}
