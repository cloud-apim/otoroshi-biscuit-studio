package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{AttenuatorConfig, BiscuitAttenuator, BiscuitExtractorConfig, RemoteFactsData}
import org.biscuitsec.biscuit.crypto.PublicKey
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.env.Env
import otoroshi.next.models.NgTlsConfig
import otoroshi.next.plugins.api._
import otoroshi.utils.syntax.implicits.{BetterJsReadable, BetterJsValue, BetterSyntax}
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
  val configFlow: Seq[String] = Seq("api_url", "api_method", "api_headers", "api_timeout", "token_replace_loc", "token_replace_name", "token_resp_loc")
  val format = new Format[BiscuitRemoteTokenFetcherConfig] {
    override def writes(o: BiscuitRemoteTokenFetcherConfig): JsValue = Json.obj(
      "api_url" -> o.apiUrl,
      "api_method" -> o.method,
      "api_headers" -> o.headers,
      "api_timeout" -> o.timeout.toMillis,
      "api_tls_config" -> o.tlsConfig.json,
      "token_replace_loc" -> o.tokenReplaceLoc,
      "token_replace_name" -> o.tokenReplaceName,
      "token_resp_loc" -> o.tokenRespLoc
    )

    override def reads(json: JsValue): JsResult[BiscuitRemoteTokenFetcherConfig] = Try {
      BiscuitRemoteTokenFetcherConfig(
        apiUrl = json.select("api_url").asOpt[String].orElse(json.select("apiUrl").asOpt[String]).getOrElse(""),
        tlsConfig = json.select("api_tls_config").asOpt[JsObject].flatMap(o => NgTlsConfig.format.reads(o).asOpt).getOrElse(NgTlsConfig()),
        headers = json.select("api_headers").asOpt[Map[String, String]].getOrElse(Map.empty),
        timeout = json.select("api_timeout").asOpt[Long].map(_.millis).getOrElse(10.seconds),
        method = json.select("api_method").asOpt[String].getOrElse("POST"),
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
      "type" -> "text",
      "label" -> "API URL"
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

  private def fetchToken(config: BiscuitRemoteTokenFetcherConfig)(implicit env: Env, ec: ExecutionContext, mat: Materializer): Future[Either[String, Option[String]]] = {
    val withBody = config.method.toUpperCase == "POST" || config.method.toUpperCase  == "PUT" || config.method.toUpperCase  == "PATCH"

    env.MtlsWs
      .url(config.apiUrl, config.tlsConfig.legacy)
      .withHttpHeaders(
        config.headers.toSeq: _*
      )
      .withMethod(config.method.toUpperCase)
      .applyOnIf(withBody) { builder =>
        builder.withBody(Json.obj("context" -> None))
      }
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
    val config = ctx.cachedConfig(internalName)(BiscuitRemoteTokenFetcherConfig.format).getOrElse(BiscuitRemoteTokenFetcherConfig())

    fetchToken(config).flatMap{
      case Left(err) => Left(Results.BadRequest(s"unable to fetch token from remote - ${err}")).vfuture
      case Right(tokenOpt) => {
        tokenOpt match {
          case None => Left(Results.BadRequest("token not found from remote API fetcher")).vfuture
          case Some(token) => {
            var finalRequest = ctx.otoroshiRequest
            config.tokenReplaceLoc match {
              case "header" => finalRequest.copy(headers = finalRequest.headers ++ Map(config.tokenReplaceName -> s"${token}")).right.vfuture
              case "query" => {
                val uri = finalRequest.uri
                val newQuery = uri.rawQueryString.map(_ => uri.query().toString()).getOrElse("") ++ s"${config.tokenReplaceName}=${token}"
                val newUrl = uri.copy(rawQueryString = newQuery.some).toString()
                finalRequest.copy(url = newUrl).right.vfuture
              }
              case "cookie" => {
                val cookie = DefaultWSCookie(name = config.tokenReplaceName, value = s"${token}", maxAge = Some(360000), path = "/".some, domain = ctx.request.domain.some, httpOnly = false)

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