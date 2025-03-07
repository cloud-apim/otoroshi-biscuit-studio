package com.cloud.apim.otoroshi.extensions.biscuit.suites

import com.cloud.apim.otoroshi.extensions.biscuit.BiscuitStudioOneOtoroshiClusterPerSuite
import org.joda.time.DateTime
import otoroshi.utils.syntax.implicits.{BetterFuture, BetterJsValue}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.libs.json.Json

import scala.concurrent.duration.DurationInt

class RevocationSuite extends BiscuitStudioOneOtoroshiClusterPerSuite {

  test("should revoke on all cluster"){
    val revokedTokenBody = Some(Json.arr(
      Json.obj(
        "id" -> "d46cd76fc718894bc21036ee4cacb001632cfa6206389e2ee2e290ab34443bef85ae29189d0e8e6236d50ae010d65beb3c5d10b278e026d4828008802bed190c",
        "reason" -> "fraud",
        "revocation_date" -> DateTime.now().toString()
      ))
    )

    // Used token
    // Eo0BCiMKBDEyMzQYAyIJCgcIChIDGIAIMg4KDAoCCBsSBggDEgIYABIkCAASIHS1xEsEbwjjsW390Tu0Iz6JWHB_XVQD9eCq9pncR3zYGkDx1E1fOsbnQVWqJLClKouVgSz7zWto0zUB8Y01GjUi9IrOTZHlkl6zpdoiqt5Xml17yK2M-egQNbLTvYRMw3cFGoIBChgKA2RldhgDMg8KDQoCCBsSBwgGEgMYgQgSJAgAEiAkqKaZsLUXlWjGQ6AHcTWa22JLhVBSAaatzAhfTjMcyBpA1GzXb8cYiUvCEDbuTKywAWMs-mIGOJ4u4uKQqzREO--FrikYnQ6OYjbVCuAQ1lvrPF0QsnjgJtSCgAiAK-0ZDCIiCiCFDocKFnhkO3y5aHFVDYsow0YitpllvSoeFlr50Fm40Q==


    // Call worker 2 api to revoke a token
    val resRevocation = workerClient2.call("POST", s"http://otoroshi-api.oto.tools:${workerPort2}/api/extensions/biscuit/tokens/revocation/_revoke",
      Map(
        "Content-Type" -> s"application/json",
        "Otoroshi-Client-Id" -> "admin-api-apikey-id",
        "Otoroshi-Client-Secret" -> "admin-api-apikey-secret"
      ), revokedTokenBody).awaitf(5.seconds)


    assertEquals(resRevocation.status, 200, "status should be 200")
    assert(resRevocation.json.at("total_revoked").isDefined, "total_revoked should be defined")
    assertEquals(resRevocation.json.at("total_revoked").as[Int], 1, "total_revoked nb should be 1")

    await(5.seconds)
    var allRvkTokensWrk1 = envWorker.adminExtensions.extension[BiscuitExtension].get.states.allRevokedTokens()
    var allRvkTokensWrk2 = envWorker2.adminExtensions.extension[BiscuitExtension].get.states.allRevokedTokens()
    var allRvkTokensLeader = env.adminExtensions.extension[BiscuitExtension].get.states.allRevokedTokens()

    println(s"allRvkTokens WRK1 = ${allRvkTokensWrk1}")
    println(s"allRvkTokens LEAD = ${allRvkTokensLeader}")
    println(s"allRvkTokens WRK2 = ${allRvkTokensWrk2}")

//    assertEquals(allRvkTokensLeader.size, 0, "revoked tokens should be empty on worker 2")
//    assertEquals(allRvkTokensLeader.size, 0, "revoked tokens should be empty on cluster leader")
//    assertEquals(allRvkTokensWrk1.size, 1, "revoked tokens should not be empty on worker 1")

    await(20.seconds)

    allRvkTokensWrk2 = envWorker2.adminExtensions.extension[BiscuitExtension].get.states.allRevokedTokens()
    println(s"allRvkTokens size = ${allRvkTokensWrk2.size}")
    println(s"allRvkTokens = ${allRvkTokensWrk2}")

    val revTokenWrk2 =  envWorker2.adminExtensions.extension[BiscuitExtension].get.states.biscuitRevokedTokens("d46cd76fc718894bc21036ee4cacb001632cfa6206389e2ee2e290ab34443bef85ae29189d0e8e6236d50ae010d65beb3c5d10b278e026d4828008802bed190c")
    val revTokenLeader =  envWorker2.adminExtensions.extension[BiscuitExtension].get.states.biscuitRevokedTokens("d46cd76fc718894bc21036ee4cacb001632cfa6206389e2ee2e290ab34443bef85ae29189d0e8e6236d50ae010d65beb3c5d10b278e026d4828008802bed190c")

    println(s"got revtoken on worker = ${revTokenWrk2}")
    println(s"got revtoken on leader = ${revTokenLeader}")

    assert(revTokenWrk2.isDefined, "token should be defined")
    assertEquals(revTokenWrk2.get.revocationId, "d46cd76fc718894bc21036ee4cacb001632cfa6206389e2ee2e290ab34443bef85ae29189d0e8e6236d50ae010d65beb3c5d10b278e026d4828008802bed190c", "token id should not be wrong")

    assert(revTokenLeader.isDefined, "token should be defined")
    assertEquals(revTokenLeader.get.revocationId, "d46cd76fc718894bc21036ee4cacb001632cfa6206389e2ee2e290ab34443bef85ae29189d0e8e6236d50ae010d65beb3c5d10b278e026d4828008802bed190c", "token id should not be wrong")
  }
}