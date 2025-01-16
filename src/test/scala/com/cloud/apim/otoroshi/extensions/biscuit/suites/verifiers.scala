package com.cloud.apim.otoroshi.extensions.biscuit.suites

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.domains.{BiscuitKeyPairsUtils, BiscuitVerifiersUtils}
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitKeyPair, BiscuitVerifier, VerifierConfig}
import com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionSuite, OtoroshiClient}
import otoroshi.api.Otoroshi
import otoroshi.models.EntityLocation
import otoroshi.utils.syntax.implicits.BetterSyntax

import scala.concurrent.ExecutionContext

class TestVerifiers extends BiscuitExtensionSuite {
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

  val keypairID = s"biscuit-verifier_7086fb05-0a0b-4be8-92b0-1f89ab243a83"

  val conf = VerifierConfig(
    checks = List.empty,
    facts = List.empty,
    resources = List.empty,
    rules = List.empty,
    policies = List.empty,
    revokedIds = List.empty
  )

  val verifier = BiscuitVerifier(
    id = keypairID,
    name = "New Biscuit Verifier entity",
    description = "New biscuit Verifier entity",
    metadata = Map.empty,
    tags = Seq.empty,
    location = EntityLocation.default,
    keypairRef = "",
    config =  conf.some
  )

  test(s"create verifier entity") {
    printHeader(verifier.name, "Create new verifier entity")
    BiscuitVerifiersUtils.createVerifierEntity(client)(verifier)
  }
}
