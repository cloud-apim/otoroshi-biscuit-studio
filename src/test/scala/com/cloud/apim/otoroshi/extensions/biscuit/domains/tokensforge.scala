package com.cloud.apim.otoroshi.extensions.biscuit.domains

import com.cloud.apim.otoroshi.extensions.biscuit.OtoroshiClient
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitTokenForge
import munit.Assertions
import otoroshi.utils.syntax.implicits.{BetterFuture, BetterJsValue}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object BiscuitTokensForgeUtils extends Assertions {

  def createKeypairEntity(client: OtoroshiClient)(token: BiscuitTokenForge)(implicit ec: ExecutionContext) = {
    val res = client.forBiscuitEntity("tokens-forge").createEntity(token).awaitf(10.seconds)

    assert(res.created, s"[${token.id}] token has not been created")
  }
}