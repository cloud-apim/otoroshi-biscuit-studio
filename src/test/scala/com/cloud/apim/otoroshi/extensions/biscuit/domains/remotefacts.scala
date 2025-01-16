package com.cloud.apim.otoroshi.extensions.biscuit.domains

import com.cloud.apim.otoroshi.extensions.biscuit.OtoroshiClient
import com.cloud.apim.otoroshi.extensions.biscuit.entities.RemoteFactsLoader
import munit.Assertions
import otoroshi.utils.syntax.implicits.{BetterFuture, BetterJsValue}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object BiscuitRemoteFactsLoaderUtils extends Assertions {

  def createKeypairEntity(client: OtoroshiClient)(remoteFacts: RemoteFactsLoader)(implicit ec: ExecutionContext) = {
    val res = client.forBiscuitEntity("biscuit-remote-facts").createEntity(remoteFacts).awaitf(10.seconds)

    assert(res.created, s"[${remoteFacts.id}] entity RemoteFactsLoader has not been created")
  }
}