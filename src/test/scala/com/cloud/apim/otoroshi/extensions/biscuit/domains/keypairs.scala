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
    assertEquals(keypairRes.resp.json.at("id").asOpt[String].getOrElse(""), "biscuit-keypair_97930360-d343-461d-98d1-8505b5ccf2f0", "keypair id not matching with provided id")
    assertEquals(keypairRes.resp.json.at("pubKey").asOpt[String].getOrElse(""), "CA31F831E8A750EF0E9C5F8B3D0CA1265428BFA9CB506CFCF4B8E883168B13C8", s"public key is not same as provided")
    assertEquals(keypairRes.resp.json.at("privKey").asOpt[String].getOrElse(""), "B09F278A4E37DBCC0AC9C10F941DFB90B5A59D3678C0B33ED76C47F760E57DB2", s"private key is not same as provided")
  }
}