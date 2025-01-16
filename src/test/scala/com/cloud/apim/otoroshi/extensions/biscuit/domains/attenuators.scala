package com.cloud.apim.otoroshi.extensions.biscuit.domains

import com.cloud.apim.otoroshi.extensions.biscuit.OtoroshiClient
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitAttenuator}
import munit.Assertions
import otoroshi.utils.syntax.implicits.{BetterFuture, BetterJsValue}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object BiscuitAttenuatorsUtils extends Assertions {

  def createKeypairEntity(client: OtoroshiClient)(attenuator: BiscuitAttenuator)(implicit ec: ExecutionContext) = {
    val res = client.forBiscuitEntity("biscuit-attenuators").createEntity(attenuator).awaitf(10.seconds)

    assert(res.created, s"[${attenuator.id}] attenuator has not been created")
  }
}