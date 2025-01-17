package com.cloud.apim.otoroshi.extensions.biscuit.domains

import com.cloud.apim.otoroshi.extensions.biscuit.OtoroshiClient
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitKeyPair
import munit.Assertions
import otoroshi.utils.syntax.implicits.{BetterFuture, BetterJsValue}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object BiscuitKeyPairsUtils extends Assertions {

  def createKeypairEntity(client: OtoroshiClient)(keypair: BiscuitKeyPair)(implicit ec: ExecutionContext) = {
    val keypairRes = client.forBiscuitEntity("biscuit-keypairs").createEntity(keypair).awaitf(10.seconds)

    assert(keypairRes.created, s"[${keypair.id}] keypair has not been created")
    assert(keypairRes.resp.json.at("pubKey").isDefined, s"[${keypair.id}] keypair public key is missing")
    assert(keypairRes.resp.json.at("privKey").isDefined, s"[${keypair.id}] keypair private key is missing")
    assertEquals(keypairRes.resp.json.at("id").asOpt[String].getOrElse(""), keypair.id, "keypair id not matching with provided id")
    assertEquals(keypairRes.resp.json.at("pubKey").asOpt[String].getOrElse(""), keypair.pubKey, s"public key is not same as provided")
    assertEquals(keypairRes.resp.json.at("privKey").asOpt[String].getOrElse(""), keypair.privKey, s"private key is not same as provided")
  }
}