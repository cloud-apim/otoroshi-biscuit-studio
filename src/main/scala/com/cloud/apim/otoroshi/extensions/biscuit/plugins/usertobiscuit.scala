package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitTokenForge
import otoroshi.el.GlobalExpressionLanguage
import otoroshi.env.Env
import otoroshi.next.plugins.api._
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.libs.json._
import play.api.mvc.{Result, Results}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Left, Success, Try}

case class UserToBiscuitExtractorConfig(
  automaticFacts: Boolean = true,
  forgeRef: String = "",
  extractorHeaderName: String = "biscuit-auth-user"
) extends NgPluginConfig {
  def json: JsValue = UserToBiscuitExtractorConfig.format.writes(this)
}

object UserToBiscuitExtractorConfig {
  val configFlow: Seq[String] = Seq("forge_ref", "extractor_header_name", "auto_facts")
  val format = new Format[UserToBiscuitExtractorConfig] {
    override def writes(o: UserToBiscuitExtractorConfig): JsValue = Json.obj(
      "auto_facts" -> o.automaticFacts,
      "forge_ref" -> o.forgeRef,
      "extractor_header_name"-> o.extractorHeaderName
    )

    override def reads(json: JsValue): JsResult[UserToBiscuitExtractorConfig] = Try {
      UserToBiscuitExtractorConfig(
        automaticFacts = json.select("auto_facts").asOpt[Boolean].getOrElse(true),
        forgeRef = json.select("forge_ref").asOpt[String].getOrElse(""),
        extractorHeaderName = json.select("extractor_header_name").asOpt[String].getOrElse(""),
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }

  def configSchema: Option[JsObject] = Some(Json.obj(
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
    "auto_facts" -> Json.obj(
      "type" -> "bool",
      "label" -> "Automatic user facts",
      "help" -> "insert user facts automatically even if not defined in the biscuit forge"
    ),
  ))
}

class UserToBiscuitExtractor extends NgRequestTransformer {

  override def name: String = "Cloud APIM - User to Biscuit Extractor"

  override def description: Option[String] = "This plugin will forge a token from an authenticated user".some

  override def defaultConfigObject: Option[NgPluginConfig] = Some(UserToBiscuitExtractorConfig())

  override def core: Boolean = false

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = UserToBiscuitExtractorConfig.configFlow

  override def configSchema: Option[JsObject] = UserToBiscuitExtractorConfig.configSchema

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand

  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Studio"), NgPluginCategory.Authentication)

  override def steps: Seq[NgStep] = Seq(NgStep.TransformRequest)

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Cloud APIM - User to Biscuit Extractor' plugin is available !")
      
    }
    ().vfuture
  }

  override def transformRequest(ctx: NgTransformerRequestContext)(implicit env: Env, ec: ExecutionContext, mat: Materializer): Future[Either[Result, NgPluginHttpRequest]] = {
    val config = ctx.cachedConfig(internalName)(UserToBiscuitExtractorConfig.format).getOrElse(UserToBiscuitExtractorConfig())
    if (ctx.user.isDefined) {
      env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitTokenForge(config.forgeRef)) match {
        case None => Left(Results.InternalServerError(Json.obj("error" -> "forge_ref not found"))).vfuture
        case Some(forge) => {
          val strForge = forge.json.stringify
          val finalForge = if (strForge.contains("${")) {
            val jsonStr = GlobalExpressionLanguage.apply(
              value = strForge,
              req = ctx.request.some,
              service = None,
              route = ctx.route.some,
              apiKey = ctx.apikey,
              user = ctx.user,
              context = ctx.attrs.get(otoroshi.plugins.Keys.ElCtxKey).getOrElse(Map.empty),
              attrs = ctx.attrs,
              env = env,
            )
            BiscuitTokenForge.format.reads(jsonStr.parseJson).get
          } else {
            forge
          }
          finalForge.forgeToken(Json.obj(), if (config.automaticFacts) ctx.user else None).flatMap {
            case Left(err) => ctx.otoroshiRequest.right.vfuture
            case Right(token) => {
              val finalRequest = ctx.otoroshiRequest
              val lowerName = config.extractorHeaderName.toLowerCase().trim
              val (headerName, prefix) = lowerName match {
                case "authorizationbearer" => ("Authorization", "Bearer ")
                case "authorization-bearer" => ("Authorization", "Bearer ")
                case "authorization:bearer" => ("Authorization", "Bearer ")
                case "authorization: bearer" => ("Authorization", "Bearer ")
                case "authorizationbiscuit" => ("Authorization", "Biscuit ")
                case "authorization-biscuit" => ("Authorization", "Biscuit ")
                case "authorization:biscuit" => ("Authorization", "Biscuit ")
                case "authorization: biscuit" => ("Authorization", "Biscuit ")
                case _ => (config.extractorHeaderName.trim, "")
              }
              val tokB64 = token.serialize_b64url()
              val context = ctx.attrs.get(otoroshi.plugins.Keys.ElCtxKey).getOrElse(Map.empty)
              if (headerName.isEmpty) {
                val newContext = context ++ Map("user_to_biscuit_token" -> tokB64)
                ctx.attrs.put(otoroshi.plugins.Keys.ElCtxKey -> newContext)
                finalRequest.right.vfuture
              } else {
                val newContext = context ++ Map(headerName -> tokB64, "user_to_biscuit_token" -> tokB64)
                ctx.attrs.put(otoroshi.plugins.Keys.ElCtxKey -> newContext)
                finalRequest.copy(
                  headers = finalRequest.headers ++ Map(headerName -> s"${prefix}${tokB64}")
                ).right.vfuture
              }
            }
          }
        }
      }
    } else {
      ctx.otoroshiRequest.right.vfuture
    }
  }
}