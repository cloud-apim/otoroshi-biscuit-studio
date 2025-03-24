package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.stream.Materializer
import org.biscuitsec.biscuit.crypto.PublicKey
import otoroshi.env.Env
import otoroshi.next.plugins.api._
import otoroshi.next.proxy.NgProxyEngineError
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.libs.json._
import play.api.mvc.Results

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class BiscuitExposePubKeysPluginConfig(authorizedPublicKeys: List[String] = List.empty) extends NgPluginConfig {
  def json: JsValue = BiscuitExposePubKeysPluginConfig.format.writes(this)
}

object BiscuitExposePubKeysPluginConfig {
  val configFlow: Seq[String] = Seq("authorized_pk_list")
  val format = new Format[BiscuitExposePubKeysPluginConfig] {
    override def writes(o: BiscuitExposePubKeysPluginConfig): JsValue = Json.obj("authorized_pk_list" -> o.authorizedPublicKeys)

    override def reads(json: JsValue): JsResult[BiscuitExposePubKeysPluginConfig] = Try {
      BiscuitExposePubKeysPluginConfig(authorizedPublicKeys = json.select("authorized_pk_list").asOpt[List[String]].getOrElse(List.empty))
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }

  def configSchema: Option[JsObject] = Some(Json.obj(
    "authorized_pk_list" -> Json.obj(
      "type" -> "select",
      "label" -> s"Exposed Keys",
      "array" -> true,
      "props" -> Json.obj(
        "isClearable" -> false,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id"
        )
      )
    )
  ))
}

class ExposeBiscuitPublicKeysPlugin extends NgBackendCall {

  override def name: String = "Cloud APIM - Expose Biscuit public keys"

  override def description: Option[String] = "This plugin allows you to expose your biscuit public keys".some

  override def defaultConfigObject: Option[NgPluginConfig] = Some(BiscuitExposePubKeysPluginConfig())

  override def core: Boolean = false

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = BiscuitExposePubKeysPluginConfig.configFlow

  override def configSchema: Option[JsObject] = BiscuitExposePubKeysPluginConfig.configSchema

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand

  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Studio"))

  override def steps: Seq[NgStep] = Seq(NgStep.CallBackend)

  override def useDelegates: Boolean = false

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Biscuit - Expose Biscuit public keys plugin' plugin is available !")
    }
    ().vfuture
  }

  override def callBackend(ctx: NgbBackendCallContext, delegates: () => Future[Either[NgProxyEngineError, BackendCallResponse]])(implicit env: Env, ec: ExecutionContext, mat: Materializer): Future[Either[NgProxyEngineError, BackendCallResponse]] = {
    val config = ctx.cachedConfig(internalName)(BiscuitExposePubKeysPluginConfig.format).getOrElse(BiscuitExposePubKeysPluginConfig())
    val data = env.adminExtensions.extension[BiscuitExtension].get.states.allPublicKeyPairs(config.authorizedPublicKeys).map { keypair =>
      val publicKey = new PublicKey(keypair.getCurrentAlgo, keypair.pubKey)
      Json.obj(
        "algorithm" -> keypair.algo,
        "key_bytes" -> publicKey.toHex,
        "key_id" -> keypair.id,
        "issuer" -> "Otoroshi"
      )
    }
    Right(BackendCallResponse(NgPluginHttpResponse.fromResult(
      Results.Ok(
        Json.obj(
          "items" -> data.toList
        )
      )
    ), None)).vfuture
  }
}