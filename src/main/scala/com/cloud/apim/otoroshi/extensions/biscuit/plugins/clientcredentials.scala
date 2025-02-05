package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.stream.Materializer
import akka.util.ByteString
import org.joda.time.DateTime
import otoroshi.env.Env
import otoroshi.models.{ApiKey, EntityIdentifier, ServiceGroupIdentifier}
import otoroshi.next.plugins.api._
import otoroshi.next.proxy.NgProxyEngineError
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.libs.json._
import play.api.mvc.{Result, Results}
import play.core.parsers.FormUrlEncodedParser

import scala.concurrent.duration.{DurationInt, DurationLong, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class ClientCredentialBiscuitTokenEndpointBody(
  grantType: String,
  clientId: String,
  clientSecret: String,
  bearerKind: String,
  aud: Option[String]
)

case class ClientCredentialBiscuitTokenEndpointConfig(
    expiration: FiniteDuration,
    forgeRef: Option[String],
    allowedApikeys: Seq[String],
    allowedGroups: Seq[String],
) extends NgPluginConfig {
  override def json: JsValue = ClientCredentialBiscuitTokenEndpointConfig.format.writes(this)
}

object ClientCredentialBiscuitTokenEndpointConfig {
  val default = ClientCredentialBiscuitTokenEndpointConfig(1.hour, None, Seq.empty, Seq.empty)
  val format  = new Format[ClientCredentialBiscuitTokenEndpointConfig] {
    override def reads(json: JsValue): JsResult[ClientCredentialBiscuitTokenEndpointConfig] = Try {
      ClientCredentialBiscuitTokenEndpointConfig(
        expiration = json.select("expiration").asOpt[Long].map(_.millis).getOrElse(1.hour),
        forgeRef = json.select("forge_ref").asOpt[String].filter(_.trim.nonEmpty),
        allowedApikeys = json.select("allowed_apikeys").asOpt[Seq[String]].getOrElse(Seq.empty),
        allowedGroups = json.select("allowed_groups").asOpt[Seq[String]].getOrElse(Seq.empty),
      )
    } match {
      case Success(s) => JsSuccess(s)
      case Failure(e) => JsError(e.getMessage)
    }

    override def writes(o: ClientCredentialBiscuitTokenEndpointConfig): JsValue = Json.obj(
      "expiration" -> o.expiration.toMillis,
      "forge_ref" -> o.forgeRef,
      "allowed_apikeys" -> o.allowedApikeys,
      "allowed_groups" -> o.allowedGroups,
    )
  }
  val configFlow: Seq[String] = Seq("expiration", "forge_ref", "allowed_apikeys", "allowed_groups")
  val configSchema: Option[JsObject] = Some(Json.obj(
    "expiration" -> Json.obj(
      "type" -> "number",
      "label" -> s"Expiration",
      "suffix" -> "millis",
      "props" -> Json.obj(
        "suffix" -> "millis"
      )
    ),
    "forge_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"Forge",
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-forges",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    ),
    "allowed_apikeys" -> Json.obj(
      "type" -> "select",
      "label" -> s"Allowed apikeys",
      "array" -> true,
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/apim.otoroshi.io/v1/apikeys",
        "optionsTransformer" -> Json.obj(
          "label" -> "clientName",
          "value" -> "id",
        ),
      ),
    ),
    "allowed_groups" -> Json.obj(
      "type" -> "select",
      "label" -> s"Allowed groups",
      "array" -> true,
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/organize.otoroshi.io/v1/service-groups",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    )
  ))
}

class ClientCredentialBiscuitTokenEndpoint extends NgBackendCall {

  override def name: String = "Cloud APIM - Client credential Biscuit token endpoint"
  override def description: Option[String] =
    "This plugin provide the endpoint for the client_credential flow backed by a Biscuit access_token".some

  override def useDelegates: Boolean = false
  override def multiInstance: Boolean = true
  override def defaultConfigObject: Option[NgPluginConfig] = Some(ClientCredentialBiscuitTokenEndpointConfig.default)
  override def deprecated: Boolean = false
  override def core: Boolean = false
  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand
  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Studio"), NgPluginCategory.Authentication)
  override def steps: Seq[NgStep] = Seq(NgStep.CallBackend)
  override def noJsForm: Boolean = true
  override def configFlow: Seq[String] = ClientCredentialBiscuitTokenEndpointConfig.configFlow
  override def configSchema: Option[JsObject] = ClientCredentialBiscuitTokenEndpointConfig.configSchema

  private def handleBody(
    ctx: NgbBackendCallContext
  )(f: Map[String, String] => Future[Result])(implicit env: Env, ec: ExecutionContext): Future[Result] = {
    implicit val mat = env.otoroshiMaterializer
    val charset      = ctx.rawRequest.charset.getOrElse("UTF-8")
    ctx.request.body.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
      ctx.request.headers.get("Content-Type") match {
        case Some(ctype) if ctype.toLowerCase().contains("application/x-www-form-urlencoded") => {
          val urlEncodedString         = bodyRaw.utf8String
          val body                     = FormUrlEncodedParser.parse(urlEncodedString, charset).mapValues(_.head)
          val map: Map[String, String] = body ++ ctx.request.headers
            .get("Authorization")
            .filter(_.startsWith("Basic "))
            .map(_.replace("Basic ", ""))
            .map(v => org.apache.commons.codec.binary.Base64.decodeBase64(v))
            .map(v => new String(v))
            .filter(_.contains(":"))
            .map(_.split(":").toSeq)
            .map(v => Map("client_id" -> v.head, "client_secret" -> v.tail.mkString(":")))
            .getOrElse(Map.empty[String, String])
          f(map)
        }
        case Some(ctype) if ctype.toLowerCase().contains("application/json")                  => {
          val json                     = Json.parse(bodyRaw.utf8String).as[JsObject]
          val map: Map[String, String] = json.value.toSeq.collect {
            case (key, JsString(v))  => (key, v)
            case (key, JsNumber(v))  => (key, v.toString())
            case (key, JsBoolean(v)) => (key, v.toString)
          }.toMap ++ ctx.request.headers
            .get("Authorization")
            .filter(_.startsWith("Basic "))
            .map(_.replace("Basic ", ""))
            .map(v => org.apache.commons.codec.binary.Base64.decodeBase64(v))
            .map(v => new String(v))
            .filter(_.contains(":"))
            .map(_.split(":").toSeq)
            .map(v => Map("client_id" -> v.head, "client_secret" -> v.tail.mkString(":")))
            .getOrElse(Map.empty[String, String])
          f(map)
        }
        case _                                                                                =>
          // bad content type
          Results.Unauthorized(Json.obj("error" -> "access_denied", "error_description" -> s"Unauthorized")).future
      }
    }
  }

  private def apikeyAllowed(conf: ClientCredentialBiscuitTokenEndpointConfig, apikey: ApiKey, ctx: NgbBackendCallContext): Boolean = {
    if (conf.allowedApikeys.isEmpty && conf.allowedGroups.isEmpty) {
      apikey.authorizedOnServiceOrGroups(ctx.route.id, ctx.route.groups)
    } else {
      if (conf.allowedApikeys.contains(apikey.clientId)) {
        true
      } else {
        val apkgroups = apikey.authorizedEntities.collect {
          case ServiceGroupIdentifier(id) => id
        }
        conf.allowedGroups.exists(s => apkgroups.contains(s))
      }
    }
  }

  private def handleTokenRequest(
    ccfb: ClientCredentialBiscuitTokenEndpointBody,
    conf: ClientCredentialBiscuitTokenEndpointConfig,
    ctx: NgbBackendCallContext
  )(implicit env: Env, ec: ExecutionContext): Future[Result] =
    ccfb match {
      case ClientCredentialBiscuitTokenEndpointBody(
      "client_credentials",
      clientId,
      clientSecret,
      _,
      aud
    ) => {
        val possibleApiKey = env.datastores.apiKeyDataStore.findById(clientId)
        possibleApiKey.flatMap {
          case Some(apiKey) if apiKey.isValid(clientSecret) && apiKey.isActive() && apikeyAllowed(conf, apiKey, ctx) => {
            val forgeRef                     = apiKey.metadata.get("biscuit-forge").orElse(conf.forgeRef)
            forgeRef match {
              case None => Results.NotFound(
                Json.obj(
                  "error"             -> "not_found",
                  "error_description" -> s"forge_ref not found"
                )
              ).vfuture
              case Some(ref) => {
                env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitTokenForge(ref)) match {
                  case None =>  Results.NotFound(
                    Json.obj(
                      "error"             -> "not_found",
                      "error_description" -> s"forge not found"
                    )
                  ).vfuture
                  case Some(forge) => {
                    val newForge = forge.copy(
                      config = forge.config.copy(
                        facts = forge.config.facts ++ Seq(s"""client_id("${clientId}")""", s"""client_name("${apiKey.clientName}")""")
                          ++ apiKey.metadata.filter(_._1.startsWith("biscuit_fact_")).map(t => s"""${t._1.replaceFirst("biscuit_fact_", "")}(${t._2})""").toSeq,
                        checks = forge.config.checks ++ Seq(
                          s"""check if time($$time), $$time <= ${DateTime.now().plusMillis(conf.expiration.toMillis.toInt).toString()}"""
                        ) ++ apiKey.metadata.filter(_._1.startsWith("biscuit_check_")).values,
                        resources = apiKey.metadata.filter(_._1.startsWith("biscuit_resource_")).map(t => s"""resource(${t._2})""").toSeq,
                        rules = apiKey.metadata.filter(_._1.startsWith("biscuit_rule_")).values.toSeq,
                      )
                    ).applyOnWithOpt(aud) {
                      case(forge, aud) => forge.copy(
                        config = forge.config.copy(facts = forge.config.facts ++ Seq(s"""aud("${aud}")"""))
                      )
                    }
                    newForge.forgeToken(ctx.json.asObject ++ Json.obj("phase" -> "backend", "plugin" -> "biscuit_client_credentials")).flatMap {
                      case Left(err) => Results.NotFound(
                        Json.obj(
                          "error"             -> "internal_server_error",
                          "error_description" -> err
                        )
                      ).vfuture
                      case Right(accessToken) => {
                        println(accessToken.serialize_b64url())
                        Results
                          .Ok(
                            Json.obj(
                              "access_token" -> accessToken.serialize_b64url(),
                              "token_type"   -> "Bearer",
                              "expires_in"   -> conf.expiration.toSeconds
                            )
                          ).vfuture
                      }
                    }
                  }
                }
              }
            }
          }
          case _                                                                 =>
            Results
              .Unauthorized(Json.obj("error" -> "access_denied", "error_description" -> s"bad client credentials"))
              .vfuture
        }
      }
      case _ =>
        Results
          .BadRequest(
            Json.obj(
              "error"             -> "unauthorized_client",
              "error_description" -> s"grant type '${ccfb.grantType}' not supported !"
            )
          ).vfuture
    }

  override def callBackend(
                            ctx: NgbBackendCallContext,
                            delegates: () => Future[Either[NgProxyEngineError, BackendCallResponse]]
                          )(implicit
                            env: Env,
                            ec: ExecutionContext,
                            mat: Materializer
                          ): Future[Either[NgProxyEngineError, BackendCallResponse]] = {
    val config = ctx
      .cachedConfig(internalName)(ClientCredentialBiscuitTokenEndpointConfig.format)
      .getOrElse(ClientCredentialBiscuitTokenEndpointConfig.default)
    handleBody(ctx) { body =>
      (
        body.get("grant_type"),
        body.get("client_id"),
        body.get("client_secret"),
        body.get("bearer_kind"),
        body.get("aud")
      ) match {
        case (Some(gtype), Some(clientId), Some(clientSecret), kind, aud) =>
          handleTokenRequest(
            ClientCredentialBiscuitTokenEndpointBody(gtype, clientId, clientSecret, kind.getOrElse("biscuit"), aud),
            config,
            ctx
          )
        case e                                                                   =>
          ctx.request.headers
            .get("Authorization")
            .filter(_.startsWith("Basic "))
            .map(_.replace("Basic ", ""))
            .map(v => org.apache.commons.codec.binary.Base64.decodeBase64(v))
            .map(v => new String(v))
            .filter(_.contains(":"))
            .map(_.split(":").toSeq)
            .map(v => (v.head, v.tail.mkString(":")))
            .map { case (clientId, clientSecret) =>
              handleTokenRequest(
                ClientCredentialBiscuitTokenEndpointBody(
                  body.getOrElse("grant_type", "--"),
                  clientId,
                  clientSecret,
                  body.getOrElse("bearer_kind", "biscuit"),
                  body.get("aud")
                ),
                config,
                ctx
              )
            }
            .getOrElse {
              // bad credentials
              Results.Unauthorized(Json.obj("error" -> "access_denied", "error_description" -> s"unauthorized")).future
            }
      }
    }.map { result =>
      BackendCallResponse(
        NgPluginHttpResponse(
          result.header.status,
          result.header.headers ++ Map(
            "Content-Type"   -> result.body.contentType.getOrElse("application/json"),
            "Content-Length" -> result.body.contentLength.getOrElse("0").toString
          ),
          Seq.empty,
          result.body.dataStream
        ),
        None
      ).right[NgProxyEngineError]
    }
  }
}