package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitKeyPair
import org.biscuitsec.biscuit.crypto.KeyPair
import otoroshi.models.EntityLocation
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits.{BetterFuture, BetterJsValue}

import scala.concurrent.duration.DurationInt

class TestKeypairs extends BiscuitStudioOneOtoroshiServerPerSuite {
  test("should be able to create a biscuit keypair entity with default algorithm") {

    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
    )

    val keypairRes = client.forBiscuitEntity("biscuit-keypairs").createEntity(keypair).awaitf(5.seconds)

    assert(keypairRes.created, s"[${keypair.id}] keypair has not been created")
    assert(keypairRes.resp.json.at("pubKey").isDefined, s"public key is missing")
    assert(keypairRes.resp.json.at("privKey").isDefined, s"private key is missing")
    assert(keypairRes.resp.json.at("algo").isDefined, s"keypair algorithm is missing")
    assertEquals(keypairRes.resp.json.at("id").as[String], keypair.id, "keypair id not matching with provided id")
    assertEquals(keypairRes.resp.json.at("pubKey").as[String], keypair.pubKey, s"public key is not same as provided")
    assertEquals(keypairRes.resp.json.at("privKey").as[String], keypair.privKey, s"private key is not same as provided")
    assertEquals(keypairRes.resp.json.at("algo").as[String], "ED25519", s"algorithm not matching 'ED25519'")
  }

  test("should be able to create a biscuit keypair entity with CUSTOM algorithm") {

    val biscuitKeyPair = new KeyPair()

    val keypair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit-keypair", otoroshi.env),
      name = "New Biscuit Key Pair",
      description = "New biscuit KeyPair",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      privKey = biscuitKeyPair.toHex,
      pubKey = biscuitKeyPair.public_key().toHex,
      algo = "SECP256K1"
    )


    val keypairRes = client.forBiscuitEntity("biscuit-keypairs").createEntity(keypair).awaitf(5.seconds)

    assert(keypairRes.created, s"[${keypair.id}] keypair has not been created")
    assert(keypairRes.resp.json.at("pubKey").isDefined, s"public key is missing")
    assert(keypairRes.resp.json.at("privKey").isDefined, s"private key is missing")
    assert(keypairRes.resp.json.at("algo").isDefined, s"keypair algorithm is missing")
    assertEquals(keypairRes.resp.json.at("id").as[String], keypair.id, "keypair id not matching with provided id")
    assertEquals(keypairRes.resp.json.at("pubKey").as[String], keypair.pubKey, s"public key is not same as provided")
    assertEquals(keypairRes.resp.json.at("privKey").as[String], keypair.privKey, s"private key is not same as provided")
    assertEquals(keypairRes.resp.json.at("algo").as[String], "SECP256K1", s"algorithm not matching 'ED25519'")
  }
}
