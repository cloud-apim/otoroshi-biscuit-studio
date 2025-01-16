package com.cloud.apim.otoroshi.extensions.biscuit.suites

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.domains.BiscuitAttenuatorsUtils
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{AttenuatorConfig, BiscuitAttenuator}
import com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionSuite, OtoroshiClient}
import otoroshi.api.Otoroshi
import otoroshi.models.EntityLocation
import otoroshi.utils.syntax.implicits.BetterSyntax

import scala.concurrent.ExecutionContext

class TestAttenuators extends BiscuitExtensionSuite {
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

  val entityId = s"biscuit-attenuator_0155dcd8-bda6-4c7f-a300-0f26f46de0a6"

  val conf = AttenuatorConfig(
    checks = List.empty,
  )

  val attenuator = BiscuitAttenuator(
    id = entityId,
    name = "New Biscuit Attenuator entity",
    description = "New biscuit Attenuator entity",
    metadata = Map.empty,
    tags = Seq.empty,
    location = EntityLocation.default,
    keypairRef = "",
    config =  conf.some
  )

  test(s"create attenuator entity") {
    printHeader(attenuator.name, "Create new attenuator entity")
    BiscuitAttenuatorsUtils.createAttenuatorEntity(client)(attenuator)
  }
}
