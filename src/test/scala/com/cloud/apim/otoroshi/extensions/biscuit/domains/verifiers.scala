package com.cloud.apim.otoroshi.extensions.biscuit.domains

import com.cloud.apim.otoroshi.extensions.biscuit.OtoroshiClient
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitKeyPair, BiscuitVerifier}
import munit.Assertions
import otoroshi.utils.syntax.implicits.{BetterFuture, BetterJsValue}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object BiscuitVerifiersUtils extends Assertions {

  def createVerifierEntity(client: OtoroshiClient)(verifier: BiscuitVerifier)(implicit ec: ExecutionContext) = {
    val verifierRes = client.forBiscuitEntity("biscuit-verifiers").createEntity(verifier).awaitf(10.seconds)

    assert(verifierRes.created, s"[${verifier.id}] verifier has not been created")
  }
}