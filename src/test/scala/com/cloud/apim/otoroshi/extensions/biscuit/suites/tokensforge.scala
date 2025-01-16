package com.cloud.apim.otoroshi.extensions.biscuit.suites

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.domains.{BiscuitTokensForgeUtils, BiscuitVerifiersUtils}
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitTokenForge, BiscuitVerifier, VerifierConfig}
import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitForgeConfig
import com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionSuite, OtoroshiClient}
import otoroshi.api.Otoroshi
import otoroshi.models.EntityLocation
import otoroshi.utils.syntax.implicits.BetterSyntax

import scala.concurrent.ExecutionContext

class TestsTokensForge extends BiscuitExtensionSuite {
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

  val entityId = s"biscuit-token_bff01628-0425-4ae9-87c9-be3600e76820"

  val conf = BiscuitForgeConfig(
    checks = List.empty,
    facts = List.empty,
    resources = List.empty,
    rules = List.empty
  )

  val biscuitToken = BiscuitTokenForge(
    id = entityId,
    name = "New Biscuit Token entity",
    description = "New biscuit Token entity",
    metadata = Map.empty,
    tags = Seq.empty,
    location = EntityLocation.default,
    keypairRef = "",
    config =  conf.some,
    token = None
  )

  test(s"create token from forge entity") {
    printHeader(biscuitToken.name, "Create new token from forge entity")
    BiscuitTokensForgeUtils.createTokenEntity(client)(biscuitToken)
  }
}
