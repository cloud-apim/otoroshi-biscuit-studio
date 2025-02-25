package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.stream.Materializer
import otoroshi.env.Env
import otoroshi.next.plugins.api.{NgPluginCategory, NgPluginConfig, NgPluginHttpRequest, NgPluginVisibility, NgRequestTransformer, NgStep, NgTransformerRequestContext}
import otoroshi.utils.syntax.implicits.{BetterJsValue, BetterSyntax}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Result, Results}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Left, Success, Try}

case class UserToBiscuitExtractorConfig(
  forgeRef: String =  "",
  extractorHeaderName: String = "biscuit-auth-user"
) extends NgPluginConfig {
  def json: JsValue = UserToBiscuitExtractorConfig.format.writes(this)
}

object UserToBiscuitExtractorConfig {
  val configFlow: Seq[String] = Seq("forge_ref", "extractor_header_name")
  val format = new Format[UserToBiscuitExtractorConfig] {
    override def writes(o: UserToBiscuitExtractorConfig): JsValue = Json.obj(
      "forge_ref" -> o.forgeRef,
      "extractor_header_name"-> o.extractorHeaderName
    )

    override def reads(json: JsValue): JsResult[UserToBiscuitExtractorConfig] = Try {
      UserToBiscuitExtractorConfig(
        forgeRef = json.select("forge_ref").asOpt[String].getOrElse(""),
        extractorHeaderName = json.select("extractor_header_name").asOpt[String].getOrElse(""),
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }

  def configSchema(name: String): Option[JsObject] = Some(Json.obj(
    "forge_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"Biscuit Forge Reference",
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-forges",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    ),
    "extractor_header_name" -> Json.obj(
      "type" -> "text",
      "label" -> "Extractor Header name"
    ),
  ))
}

class UserToBiscuitExtractor extends NgRequestTransformer {

  private val logger = Logger("user-to-biscuit-extractor-plugin")

  override def name: String = "Cloud APIM - User to Biscuit Extractor"

  override def description: Option[String] = "This plugin will forge a token from an authenticated user".some

  override def defaultConfigObject: Option[NgPluginConfig] = Some(UserToBiscuitExtractorConfig())

  override def core: Boolean = false

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = UserToBiscuitExtractorConfig.configFlow

  override def configSchema: Option[JsObject] = UserToBiscuitExtractorConfig.configSchema("user-to-biscuit")

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand

  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Studio"), NgPluginCategory.Authentication)

  override def steps: Seq[NgStep] = Seq(NgStep.ValidateAccess)

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Cloud APIM - User to Biscuit Extractor' plugin is available !")
    }
    ().vfuture
  }

  override def transformRequest(ctx: NgTransformerRequestContext)(implicit env: Env, ec: ExecutionContext, mat: Materializer): Future[Either[Result, NgPluginHttpRequest]] = {
    val config = ctx.cachedConfig(internalName)(UserToBiscuitExtractorConfig.format).getOrElse(UserToBiscuitExtractorConfig())
    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitTokenForge(config.forgeRef)) match {
      case None => Left(Results.InternalServerError(Json.obj("error" -> "forge_ref not found"))).vfuture
      case Some(forge) => {
        if(ctx.user.isDefined){
          forge.forgeToken(Json.obj(), ctx.user).flatMap {
            case Left(err) => ctx.otoroshiRequest.right.vfuture
            case Right(token) => {
              var finalRequest = ctx.otoroshiRequest
              finalRequest.copy(headers = finalRequest.headers ++ Map(config.extractorHeaderName -> s"${token.serialize_b64url()}")).right.vfuture
            }
          }
        }else{
          ctx.otoroshiRequest.right.vfuture
        }
      }
    }
  }
}