package com.cloud.apim.otoroshi.extensions.biscuit.domains

import com.cloud.apim.otoroshi.extensions.biscuit.OtoroshiClient
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitRbacPolicy
import munit.Assertions
import otoroshi.utils.syntax.implicits.{BetterFuture, BetterJsValue}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object BiscuitRbacUtils extends Assertions {

  def createRbacEntity(client: OtoroshiClient)(rbacPolicy: BiscuitRbacPolicy)(implicit ec: ExecutionContext) = {
    val res = client.forBiscuitEntity("biscuit-rbac").createEntity(rbacPolicy).awaitf(10.seconds)

    assert(res.created, s"[${rbacPolicy.id}] RBAC Entity has not been created")
  }
}