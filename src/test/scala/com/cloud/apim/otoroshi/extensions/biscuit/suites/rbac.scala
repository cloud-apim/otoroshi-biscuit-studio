package com.cloud.apim.otoroshi.extensions.biscuit.suites

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.domains.BiscuitRbacUtils
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitRbacPolicy
import com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionSuite, OtoroshiClient}
import otoroshi.api.Otoroshi
import otoroshi.models.EntityLocation

import scala.concurrent.ExecutionContext

class TestRbac extends BiscuitExtensionSuite {
  def printHeader(str: String, what: String): Unit = {
    println("\n\n-----------------------------------------")
    println(s"  [${str}] - ${what}")
    println("-----------------------------------------\n\n")
  }

  val port: Int = freePort
  var otoroshi: Otoroshi = _
  var client: OtoroshiClient = _
  implicit var ec: ExecutionContext = _
  implicit var mat: Materializer = _

  override def beforeAll(): Unit = {
    otoroshi = startOtoroshiServer(port)
    client = clientFor(port)
    ec = otoroshi.executionContext
    mat = otoroshi.materializer
  }

  override def afterAll(): Unit = {
    otoroshi.stop()
  }

  val entityId = s"biscuit-rbac-policy_ef6d2c09-203e-4ce5-8519-3e931df5601a"

  val rbacEntity = BiscuitRbacPolicy(
    id = entityId,
    name = "New Biscuit RBAC entity",
    description = "New biscuit RBAC entity",
    metadata = Map.empty,
    tags = Seq.empty,
    location = EntityLocation.default,
    roles = Map.empty
  )

  test(s"create rbac entity") {
    printHeader(rbacEntity.name, "Create new attenuator entity")
    BiscuitRbacUtils.createRbacEntity(client)(rbacEntity)
  }
}
