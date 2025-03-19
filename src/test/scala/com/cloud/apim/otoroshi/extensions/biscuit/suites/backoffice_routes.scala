package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiServerPerSuite
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitKeyPair
import org.biscuitsec.biscuit.crypto.KeyPair
import otoroshi.models.EntityLocation
import otoroshi.next.models._
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.{BiscuitExposePubKeysPluginConfig, ExposeBiscuitPublicKeysPlugin}
import play.api.libs.json.{JsObject, Json}

import java.util.UUID
import scala.concurrent.duration.DurationInt

class BackofficeRoutesSuite extends BiscuitStudioOneOtoroshiServerPerSuite {

  test("should be able to generate a keypair") {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                       test endpoint                                            ///////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val resp = client.call("GET", s"http://otoroshi.oto.tools:${port}/extensions/cloud-apim/extensions/biscuit/keypairs/_generate",
      Map(
        "Content-Type" -> s"application/json"
      ), None).awaitf(5.seconds)
    assertEquals(resp.status, 200, s"verifier route did not respond with 200")
    assert(resp.json.at("done").isDefined, s"'done' field should be defined")
    assert(resp.json.at("done").asBoolean, s"request 'done' status should be true")
    assert(resp.json.at("pubKey").isDefined, "'pubKey' should be defined")
    assert(resp.json.at("privKey").isDefined, "'privKey' should be defined")
  }

}
