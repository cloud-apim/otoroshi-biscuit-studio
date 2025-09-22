package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.stream.Materializer
import otoroshi.env.Env
import otoroshi.next.models.NgTlsConfig
import otoroshi.next.plugins.api._
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.DefaultWSCookie
import play.api.mvc.{Result, Results}

import scala.concurrent.duration.{DurationInt, DurationLong, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class BiscuitRemoteTokenFetcherConfig(
  apiUrl: String = "",
  method: String = "POST",
  body: Option[String] = None,
  includeOtoroshiContext: Boolean = false,
  headers: Map[String, String] = Map(
    "Content-Type" -> "application/json",
    "Authorization" -> "Bearer ${user.tokens.access_token}"
  ),
  tlsConfig: NgTlsConfig = NgTlsConfig(),
  timeout: FiniteDuration = 10.seconds,
  tokenReplaceLoc: String = "header",
  tokenReplaceName: String = "Authorization",
  tokenRespLoc: String = "token"
) extends NgPluginConfig {
  def json: JsValue = BiscuitRemoteTokenFetcherConfig.format.writes(this)
}

object BiscuitRemoteTokenFetcherConfig {
  val configFlow: Seq[String] = Seq("api_url", "api_method", "api_headers", "api_body", "api_timeout", "otoroshi_ctx", "token_replace_loc", "token_replace_name", "token_resp_loc")
  val format = new Format[BiscuitRemoteTokenFetcherConfig] {
    override def writes(o: BiscuitRemoteTokenFetcherConfig): JsValue = Json.obj(
      "api_url" -> o.apiUrl,
      "api_method" -> o.method,
      "api_headers" -> o.headers,
      "api_timeout" -> o.timeout.toMillis,
      "api_tls_config" -> o.tlsConfig.json,
      "token_replace_loc" -> o.tokenReplaceLoc,
      "token_replace_name" -> o.tokenReplaceName,
      "token_resp_loc" -> o.tokenRespLoc,
      "otoroshi_ctx" -> o.includeOtoroshiContext,
    ).applyOnWithOpt(o.body) {
      case (obj, body) => obj ++ Json.obj("api_body" -> body)
    }

    override def reads(json: JsValue): JsResult[BiscuitRemoteTokenFetcherConfig] = Try {
      BiscuitRemoteTokenFetcherConfig(
        apiUrl = json.select("api_url").asOpt[String].orElse(json.select("apiUrl").asOpt[String]).getOrElse(""),
        tlsConfig = json.select("api_tls_config").asOpt[JsObject].flatMap(o => NgTlsConfig.format.reads(o).asOpt).getOrElse(NgTlsConfig()),
        headers = json.select("api_headers").asOpt[Map[String, String]].getOrElse(Map.empty),
        timeout = json.select("api_timeout").asOpt[Long].map(_.millis).getOrElse(10.seconds),
        method = json.select("api_method").asOpt[String].getOrElse("POST"),
        body = json.select("api_body").asOpt[String],
        includeOtoroshiContext = json.select("otoroshi_ctx").asOpt[Boolean].getOrElse(false),
        tokenReplaceLoc = json.select("token_replace_loc").asOpt[String].getOrElse("header"),
        tokenReplaceName = json.select("token_replace_name").asOpt[String].getOrElse("Authorization"),
        tokenRespLoc = json.select("token_resp_loc").asOpt[String].getOrElse("token")
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }

  def configSchema: Option[JsObject] = Some(Json.obj(
    "api_url" -> Json.obj(
      "placeholder" -> "https://my.keycloak.instance/realms/my-realm/biscuit/token",
      "type" -> "string",
      "label" -> "API URL"
    ),
    "api_body" -> Json.obj(
      "type" -> "code",
      "label" -> "API Body"
    ),
    "otoroshi_ctx" -> Json.obj(
      "type" -> "bool",
      "label" -> "Use Otoroshi Context"
    ),
    "api_method" -> Json.obj(
      "type" -> "select",
      "label" -> s"Method",
      "props" -> Json.obj(
        "options" -> Seq(
          Json.obj("label" -> "GET", "value" -> "GET"),
          Json.obj("label" -> "POST", "value" -> "POST"),
          Json.obj("label" -> "PUT", "value" -> "PUT"),
          Json.obj("label" -> "PATCH", "value" -> "PATCH"),
        )
      ),
    ),
    "api_headers" -> Json.obj(
      "type" -> "object",
      "label" -> "Headers"
    ),
    "api_timeout" -> Json.obj(
      "type" -> "number",
      "label" -> s"API Timeout",
      "suffix" -> "millis",
      "props" -> Json.obj(
        "suffix" -> "millis"
      )
    ),
    "token_replace_loc" -> Json.obj(
      "type" -> "select",
      "label" -> s"Replace location",
      "props" -> Json.obj(
        "options" -> Seq(
          Json.obj("label" -> "Headers", "value" -> "header"),
          Json.obj("label" -> "Cookies", "value" -> "cookie"),
          Json.obj("label" -> "Query params", "value" -> "query")
        )
      ),
    ),
    "token_replace_name" -> Json.obj(
      "type" -> "text",
      "label" -> "Biscuit insertion field name"
    ),
    "token_resp_loc" -> Json.obj(
      "type" -> "text",
      "label" -> "If response body contains JSON, select the location of the token"
    )
  ))
}

class BiscuitRemoteTokenFetcherPlugin extends NgRequestTransformer {

  private val logger = Logger("biscuit-remote-token-fetcher-plugin")

  override def description: Option[String] = "Plugin to fetch a remote token".some

  override def core: Boolean = false

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand

  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Studio"))

  override def steps: Seq[NgStep] = Seq(NgStep.TransformRequest)

  override def defaultConfigObject: Option[NgPluginConfig] = Some(BiscuitRemoteTokenFetcherConfig())

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = BiscuitRemoteTokenFetcherConfig.configFlow

  override def configSchema: Option[JsObject] = BiscuitRemoteTokenFetcherConfig.configSchema

  override def name: String = "Cloud APIM - Biscuit Remote Tokens Fetcher"

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Biscuit Remote Tokens Fetcher' plugin is available !")
    }
    ().vfuture
  }

  private def fetchToken(config: BiscuitRemoteTokenFetcherConfig, ctx: NgTransformerRequestContext)(implicit env: Env, ec: ExecutionContext, mat: Materializer): Future[Either[String, Option[String]]] = {
    val withBody = config.method.toUpperCase == "POST" || config.method.toUpperCase  == "PUT" || config.method.toUpperCase  == "PATCH"
    val useOtoroshiBody = withBody && config.includeOtoroshiContext
    val useUserBody = withBody && !config.includeOtoroshiContext && config.body.isDefined
    val headers = if (useOtoroshiBody) config.headers ++ Map("Content-Type" -> "application/json") else config.headers
    env.MtlsWs
      .url(config.apiUrl, config.tlsConfig.legacy)
      .withMethod(config.method.toUpperCase)
      .applyOnIf(useOtoroshiBody && !useUserBody) { builder =>
        builder.withBody(ctx.json)
      }
      .applyOnIf(!useOtoroshiBody && useUserBody) { builder =>
        builder.withBody(config.body.get)
      }
      .withHttpHeaders(headers.toSeq: _*)
      .withRequestTimeout(config.timeout)
      .execute()
      .map { resp =>
        resp.status match {
          case 200 => {
            if (resp.contentType.contains("application/json") && resp.json.at(config.tokenRespLoc).isDefined){
              Right(Some(resp.json.at(config.tokenRespLoc).asString))
            }else{
              Right(Some(resp.body))
            }
          }
          case _ => {
            Left("Unable to fetch token from API")
          }
        }
      }
  }

  override def transformRequest(ctx: NgTransformerRequestContext)(implicit env: Env, ec: ExecutionContext, mat: Materializer): Future[Either[Result, NgPluginHttpRequest]] = {
    val config_raw = ctx.cachedConfig(internalName)(BiscuitRemoteTokenFetcherConfig.format).getOrElse(BiscuitRemoteTokenFetcherConfig())
    // Apply EL on config
    val config = BiscuitRemoteTokenFetcherConfig.format.reads(config_raw.json.stringify.evaluateEl(ctx.attrs).parseJson).asOpt.getOrElse(config_raw)
    fetchToken(config, ctx).flatMap{
      case Left(err) => Left(Results.BadRequest(s"unable to fetch token from remote - ${err}")).vfuture
      case Right(tokenOpt) => {
        tokenOpt match {
          case None => Left(Results.BadRequest("token not found from remote API fetcher")).vfuture
          case Some(token) => {
            val context = ctx.attrs.get(otoroshi.plugins.Keys.ElCtxKey).getOrElse(Map.empty)
            val newContext = context ++ Map("remote_fetched_biscuit" -> token)
            ctx.attrs.put(otoroshi.plugins.Keys.ElCtxKey -> newContext)
            config.tokenReplaceLoc match {
              case "header" => ctx.otoroshiRequest.copy(headers = ctx.otoroshiRequest.headers ++ Map(config.tokenReplaceName -> s"${token}")).right.vfuture
              case "query" => {
                val uri = ctx.otoroshiRequest.uri
                val newQuery = uri.rawQueryString.map(_ => uri.query().toString()).getOrElse("") ++ s"${config.tokenReplaceName}=${token}"
                val newUrl = uri.copy(rawQueryString = newQuery.some).toString()
                ctx.otoroshiRequest.copy(url = newUrl).right.vfuture
              }
              case "cookie" => {
                val cookie = DefaultWSCookie(name = config.tokenReplaceName, value = s"${token}", maxAge = Some(360000), path = "/".some, domain = ctx.request.domain.some, httpOnly = false)
                ctx.otoroshiRequest.copy(cookies = ctx.otoroshiRequest.cookies ++ Seq(cookie)).right.vfuture
              }
              case _ => ctx.otoroshiRequest.right.vfuture
            }
          }
        }
      }
    }
  }
}