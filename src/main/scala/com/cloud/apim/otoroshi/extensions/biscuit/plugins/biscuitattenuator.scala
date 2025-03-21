package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{AttenuatorConfig, BiscuitAttenuator, BiscuitExtractorConfig}
import org.biscuitsec.biscuit.crypto.PublicKey
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.env.Env
import otoroshi.next.plugins.api._
import otoroshi.utils.syntax.implicits.{BetterJsReadable, BetterJsValue, BetterSyntax}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.DefaultWSCookie
import play.api.mvc.{Result, Results}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class BiscuitAttenuatorConfig(
  attenuatorRef: String = "",
  extractorType: String = "header",
  extractorName: String = "Authorization",
  tokenReplaceLoc: String = "header",
  tokenReplaceName: String = "Authorization",
  enableRemoteFacts: Boolean = false,
  remoteFactsRef: String = ""
) extends NgPluginConfig {
  def json: JsValue = BiscuitAttenuatorConfig.format.writes(this)
}

object BiscuitAttenuatorConfig {
  val configFlow: Seq[String] = Seq("attenuator_ref", "extractor_type", "extractor_name", "token_replace_loc", "token_replace_name", "enable_remote_facts", "remote_facts_ref")
  val format = new Format[BiscuitAttenuatorConfig] {
    override def writes(o: BiscuitAttenuatorConfig): JsValue = Json.obj(
      "attenuator_ref" -> o.attenuatorRef,
      "extractor_type" -> o.extractorType,
      "extractor_name" -> o.extractorName,
      "token_replace_loc" -> o.tokenReplaceLoc,
      "token_replace_name" -> o.tokenReplaceName,
      "enable_remote_facts" -> o.enableRemoteFacts,
      "remote_facts_ref" -> o.remoteFactsRef
    )

    override def reads(json: JsValue): JsResult[BiscuitAttenuatorConfig] = Try {
      BiscuitAttenuatorConfig(
        attenuatorRef = json.select("attenuator_ref").asOpt[String].getOrElse(""),
        extractorType = json.select("extractor_type").asOpt[String].getOrElse("header"),
        extractorName = json.select("extractor_name").asOpt[String].getOrElse("Authorization"),
        tokenReplaceLoc = json.select("token_replace_loc").asOpt[String].getOrElse("header"),
        tokenReplaceName = json.select("token_replace_name").asOpt[String].getOrElse("Authorization"),
        enableRemoteFacts = json.select("enable_remote_facts").asOpt[Boolean].getOrElse(false),
        remoteFactsRef = json.select("remote_facts_ref").asOpt[String].getOrElse(""),
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }

  def configSchema: Option[JsObject] = Some(Json.obj(
    "attenuator_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"Biscuit Attenuator",
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-attenuators",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    ),
    "extractor_type" -> Json.obj(
      "type" -> "select",
      "label" -> s"Extractor type",
      "props" -> Json.obj(
        "options" -> Seq(
          Json.obj("label" -> "Header", "value" -> "header"),
          Json.obj("label" -> "Cookies", "value" -> "cookie"),
          Json.obj("label" -> "Query params", "value" -> "query")
        )
      ),
    ),
    "extractor_name" -> Json.obj(
      "type" -> "text",
      "label" -> "Biscuit field name"
    ),
    "token_replace_loc" -> Json.obj(
      "type" -> "select",
      "label" -> s"Replace location",
      "props" -> Json.obj(
        "options" -> Seq(
          Json.obj("label" -> "Header", "value" -> "header"),
          Json.obj("label" -> "Cookies", "value" -> "cookie"),
          Json.obj("label" -> "Query params", "value" -> "query")
        )
      ),
    ),
    "token_replace_name" -> Json.obj(
      "type" -> "text",
      "label" -> "New Biscuit field name"
    ),
    "enable_remote_facts" -> Json.obj(
      "type" -> "bool",
      "label" -> "Enable Remote Facts Loader"
    ),
    "remote_facts_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"Load Remote Facts",
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-remote-facts",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    )
  ))
}

class BiscuitTokenAttenuatorPlugin extends NgRequestTransformer {

  private val logger = Logger("biscuit-token-attenuator-plugin")

  override def description: Option[String] = "Plugin to attenuate a biscuit token".some

  override def core: Boolean = false

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand

  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Studio"))

  override def steps: Seq[NgStep] = Seq(NgStep.TransformRequest)

  override def defaultConfigObject: Option[NgPluginConfig] = Some(BiscuitAttenuatorConfig())

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = BiscuitAttenuatorConfig.configFlow

  override def configSchema: Option[JsObject] = BiscuitAttenuatorConfig.configSchema

  override def name: String = "Cloud APIM - Biscuit Tokens Attenuator"

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Biscuit Attenuator' plugin is available !")
    }
    ().vfuture
  }

  override def transformRequest(ctx: NgTransformerRequestContext)(implicit env: Env, ec: ExecutionContext, mat: Materializer): Future[Either[Result, NgPluginHttpRequest]] = {
    val config = ctx.cachedConfig(internalName)(BiscuitAttenuatorConfig.format).getOrElse(BiscuitAttenuatorConfig())

    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitAttenuator(config.attenuatorRef)) match {
      case None => Left(Results.InternalServerError(Json.obj("error" -> "attenuator_ref not found in your plugin configuration"))).vfuture
      case Some(attenuator) => {
        // Verify if the remoteFacts is enabled and the entity reference is provided
        if (config.enableRemoteFacts && config.remoteFactsRef.nonEmpty) {
          env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitRemoteFactsLoader(config.remoteFactsRef)) match {
            case None => Left(Results.InternalServerError(Json.obj("error" -> "remote_facts_ref not found in your plugin configuration"))).vfuture
            case Some(remoteFactsEntity) => {
              if (remoteFactsEntity.config.apiUrl.nonEmpty && remoteFactsEntity.config.headers.nonEmpty) {
                remoteFactsEntity.config.getRemoteFacts(ctx.json.asObject ++ Json.obj("phase" -> "access", "plugin" -> "biscuit_attenuator")).flatMap {
                  case Left(error) => Left(Results.InternalServerError(Json.obj("error" -> s"Unable to get remote facts: ${error}"))).vfuture
                  case Right(factsData) => {
                    val attenuatorConfigWithRemoteFacts = attenuator.config.copy(checks = attenuator.config.checks ++ factsData.checks)

                    doAttenuation(ctx, config, attenuator, attenuatorConfigWithRemoteFacts)
                  }
                }
              } else {
                Left(Results.InternalServerError(Json.obj("error" -> "bad remoteFacts entity configuration"))).vfuture
              }
            }
          }
        } else {
          doAttenuation(ctx, config, attenuator, attenuator.config)
        }
      }
    }
  }

  def doAttenuation(ctx: NgTransformerRequestContext, config: BiscuitAttenuatorConfig, attenuator: BiscuitAttenuator, attenuatorConfig: AttenuatorConfig)(implicit env: Env, ec: ExecutionContext, mat: Materializer): Future[Either[Result, NgPluginHttpRequest]] = {
    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(attenuator.keypairRef)) match {
      case None => Left(Results.InternalServerError(Json.obj("error" -> "keypair entity not found"))).vfuture
      case Some(keypair) => {
        val publicKey = new PublicKey(biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519, keypair.pubKey)
        BiscuitExtractorConfig(config.extractorType, config.extractorName).extractToken(ctx.request) match {
          case None => Left(Results.InternalServerError(Json.obj("error" -> "token not found from header"))).vfuture
          case Some(token) => {
            Try(Biscuit.from_b64url(token, publicKey)).toEither match {
              case Left(error) => Left(Results.InternalServerError(Json.obj("error" -> s"Unable to deserialize biscuit token - ${error}"))).vfuture
              case Right(biscuitUnverified) =>
                Try(biscuitUnverified.verify(publicKey)).toEither match {
                  case Left(error) => Left(Results.InternalServerError(Json.obj("error" -> s"Unable to verify biscuit token - token is not valid : ${error}"))).vfuture
                  case Right(biscuitToken) => {
                    AttenuatorConfig(attenuatorConfig.checks).attenuate(biscuitToken) match {
                      case Left(err) => Left(Results.InternalServerError(Json.obj("error" -> s"Unable to generate an attenuated biscuit token : ${err}"))).vfuture
                      case Right(attenuatedToken) => {

                        var finalRequest = ctx.otoroshiRequest

                        config.extractorType match {
                          case "header" => finalRequest = finalRequest.copy(headers = finalRequest.headers.filterNot(_._1.toLowerCase() == config.extractorName.toLowerCase()))
                          case "query" => {
                            val uri = finalRequest.uri
                            val newQuery = uri.rawQueryString.map(_ => uri.query().filterNot(_._1.toLowerCase() == config.extractorName.toLowerCase()).toString())
                            val newUrl = uri.copy(rawQueryString = newQuery).toString()
                            finalRequest = finalRequest.copy(url = newUrl)
                          }
                          case "cookie" => {
                            finalRequest = finalRequest.copy(cookies = ctx.otoroshiRequest.cookies.filterNot(_.name.toLowerCase() == config.extractorName.toLowerCase()))
                          }
                        }

                        config.tokenReplaceLoc match {
                          case "header" => finalRequest.copy(headers = finalRequest.headers ++ Map(config.tokenReplaceName -> s"biscuit:${attenuatedToken.serialize_b64url()}")).right.vfuture
                          case "query" => {
                            val uri = finalRequest.uri
                            val newQuery = uri.rawQueryString.map(_ => uri.query().filterNot(_._1.toLowerCase() == config.extractorName.toLowerCase()).toString()).getOrElse("") ++ s"${config.tokenReplaceName}=biscuit:${attenuatedToken.serialize_b64url()}"
                            val newUrl = uri.copy(rawQueryString = newQuery.some).toString()
                            finalRequest.copy(url = newUrl).right.vfuture
                          }
                          case "cookie" => {
                            val cookie = DefaultWSCookie(name = config.tokenReplaceName, value = s"biscuit:${attenuatedToken.serialize_b64url()}", maxAge = Some(360000), path = "/".some, domain = ctx.request.domain.some, httpOnly = false)

                            finalRequest.copy(cookies = finalRequest.cookies ++ Seq(cookie)).right.vfuture
                          }
                          case _ => finalRequest.right.vfuture
                        }
                      }
                    }
                  }
                }
            }
          }
        }
      }
    }
  }
}