package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.Done
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitExtractorConfig
import org.biscuitsec.biscuit.token.Biscuit
import org.biscuitsec.biscuit.token.builder.Term.Str
import otoroshi.env.Env
import otoroshi.next.plugins.api._
import otoroshi.utils.syntax.implicits.{BetterJsValue, BetterSyntax}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Results

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.{asScalaBufferConverter, asScalaSetConverter}
import scala.util.{Failure, Success, Try}

case class BiscuitApiKeyBridgeConfig(
  keypairRef: String = "",
  enforce: Boolean = true,
  extractorType: String = "header",
  extractorName: String = "Authorization",
  clientIdKey: String = "client_id"
) extends NgPluginConfig {
  def json: JsValue = BiscuitApiKeyBridgeConfig.format.writes(this)
}

object BiscuitApiKeyBridgeConfig {
  val configFlow: Seq[String] = Seq("keypair_ref", "enforce", "extractor_type", "extractor_name", "client_id_key")
  val format = new Format[BiscuitApiKeyBridgeConfig] {
    override def writes(o: BiscuitApiKeyBridgeConfig): JsValue = Json.obj(
      "keypair_ref" -> o.keypairRef,
      "enforce" -> o.enforce,
      "extractor_type" -> o.extractorType,
      "extractor_name" -> o.extractorName,
      "client_id_key" -> o.clientIdKey
    )

    override def reads(json: JsValue): JsResult[BiscuitApiKeyBridgeConfig] = Try {
      BiscuitApiKeyBridgeConfig(
        keypairRef = json.select("keypair_ref").asOpt[String].getOrElse(""),
        enforce = json.select("enforce").asOpt[Boolean].getOrElse(true),
        extractorType = json.select("extractor_type").asOpt[String].getOrElse(""),
        extractorName = json.select("extractor_name").asOpt[String].getOrElse(""),
        clientIdKey = json.select("client_id_key").asOpt[String].getOrElse("")
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }

  def configSchema: Option[JsObject] = Some(Json.obj(
    "keypair_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"Biscuit Keypair Reference",
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    ),
    "enforce" -> Json.obj(
      "type" -> "bool",
      "label" -> "Enforce"
    ),
    "extractor_type" -> Json.obj(
      "type" -> "select",
      "label" -> s"Extractor type",
      "props" -> Json.obj(
        "options" -> Seq(
          Json.obj("label" -> "Headers", "value" -> "header"),
          Json.obj("label" -> "Cookies", "value" -> "cookie"),
          Json.obj("label" -> "Query params", "value" -> "query")
        )
      ),
    ),
    "extractor_name" -> Json.obj(
      "type" -> "text",
      "label" -> "Biscuit field name"
    ),
    "client_id_key" -> Json.obj(
      "type" -> "text",
      "label" -> "Client Id Key"
    )
  ))
}

class BiscuitApiKeyBridgePlugin extends NgPreRouting {

  private val logger = Logger("biscuit-apikey-bridge-plugin")

  override def name: String = "Cloud APIM - Biscuit ApiKey bridge"

  override def description: Option[String] = "This plugin validates a Biscuit token".some

  override def defaultConfigObject: Option[NgPluginConfig] = Some(BiscuitApiKeyBridgeConfig())

  override def core: Boolean = false

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = BiscuitApiKeyBridgeConfig.configFlow

  override def configSchema: Option[JsObject] = BiscuitApiKeyBridgeConfig.configSchema

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand

  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Studio"), NgPluginCategory.AccessControl)

  override def steps: Seq[NgStep] = Seq(NgStep.ValidateAccess)

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Biscuit - token/apikey bridge' plugin is available !")
    }
    ().vfuture
  }

  override def preRoute(
    ctx: NgPreRoutingContext
  )(implicit env: Env, ec: ExecutionContext): Future[Either[NgPreRoutingError, Done]] = {

    val config = ctx.cachedConfig(internalName)(BiscuitApiKeyBridgeConfig.format).getOrElse(BiscuitApiKeyBridgeConfig())
    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(config.keypairRef)) match {
      case None => handleError("keypair_ref not found")
      case Some(keypair) => {
        BiscuitExtractorConfig(config.extractorType, config.extractorName).extractToken(ctx.request) match {
          case Some(token) => {
            Try(Biscuit.from_b64url(token, keypair.getPubKey)).toEither match {
              case Left(err) => handleError(s"Unable to deserialize Biscuit token : ${err}")
              case Right(biscuitUnverified) =>

                Try(biscuitUnverified.verify(keypair.getPubKey)).toEither match {
                  case Left(err) => handleError(s"Biscuit token is not valid : ${err}")
                  case Right(biscuitToken) => {
                    extractApiKey(ctx, biscuitToken, config)
                  }
                }
            }
          }
          case None if config.enforce => unauthorized(Json.obj("error" -> "unauthorized", "error_description" -> "Biscuit not found in request"))
          case None if !config.enforce => Done.right.vfuture
        }
      }
    }
  }

  def extractApiKey(ctx: NgPreRoutingContext, biscuitToken: Biscuit, config: BiscuitApiKeyBridgeConfig)(implicit env: Env, ec: ExecutionContext): Future[Either[NgPreRoutingError, Done]] = {
    val otoroshiClientID = biscuitToken.authorizer().query(s"api_key_client_id($$id) <- ${config.clientIdKey}($$id)")

    val client_id: Option[String] = Try(otoroshiClientID).toOption
      .map(_.asScala)
      .flatMap(_.headOption)
      .filter(_.name() == "api_key_client_id")
      .map(_.terms().asScala)
      .flatMap(_.headOption)
      .flatMap {
        case str: Str => str.getValue.some
        case _ => None
      }

    client_id match {
      case None => Done.right.vfuture
      case Some(clientId) => {
        env.datastores.apiKeyDataStore.findById(clientId).flatMap {
          case Some(apikey) if apikey.isInactive() && config.enforce => unauthorized(Json.obj("error" -> "unauthorized", "error_description" -> "ApiKey is inactive"))
          case Some(apikey) if apikey.isInactive() => Done.right.vfuture
          case Some(apikey) => {
            ctx.attrs.put(otoroshi.plugins.Keys.ApiKeyKey -> apikey)

            Done.right.vfuture
          }
          case _ => unauthorized(Json.obj("error" -> "unauthorized", "error_description" -> "Api Key (based on biscuit fact 'client_id') doesn't exist"))
        }
      }
    }
  }

  def unauthorized(error: JsObject): Future[Either[NgPreRoutingError, Done]] = {
    NgPreRoutingErrorWithResult(Results.Unauthorized(error)).leftf
  }

  def handleError(message: String): Future[Either[NgPreRoutingError, Done]] =
    NgPreRoutingErrorWithResult(Results.InternalServerError(Json.obj("error" -> message))).leftf
}