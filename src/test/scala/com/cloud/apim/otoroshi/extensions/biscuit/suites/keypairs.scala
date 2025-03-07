package com.cloud.apim.otoroshi.extensions.biscuit.suites

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.domains.BiscuitKeyPairsUtils
import com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionSuite, OtoroshiClient}
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitKeyPair
import otoroshi.api.Otoroshi
import otoroshi.models.EntityLocation
import scala.concurrent.ExecutionContext
class TestKeypairs extends BiscuitExtensionSuite {
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

  val keypairID = s"biscuit-keypair_97930360-d343-461d-98d1-8505b5ccf2f0"

  val demoKeyPair = BiscuitKeyPair(
    id = keypairID,
    name = "New Biscuit Key Pair",
    description = "New biscuit KeyPair",
    metadata = Map.empty,
    tags = Seq.empty,
    location = EntityLocation.default,
    pubKey = "CA31F831E8A750EF0E9C5F8B3D0CA1265428BFA9CB506CFCF4B8E883168B13C8",
    privKey = "B09F278A4E37DBCC0AC9C10F941DFB90B5A59D3678C0B33ED76C47F760E57DB2"
  )

  test(s"create keypair") {
    printHeader(demoKeyPair.name, "Create new keypair")
    BiscuitKeyPairsUtils.createKeypairEntity(client)(demoKeyPair)
  }
}
