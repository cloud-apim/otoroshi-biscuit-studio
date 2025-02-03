package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitRemoteUtils, BiscuitUtils}
import org.biscuitsec.biscuit.crypto.PublicKey
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.env.Env
import otoroshi.gateway.Errors
import otoroshi.next.plugins.AccessValidatorContext
import otoroshi.next.plugins.api._
import otoroshi.utils.syntax.implicits.{BetterJsReadable, BetterJsValue, BetterSyntax}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.Logger
import play.api.libs.json.{Format, JsError, JsObject, JsResult, JsSuccess, JsValue, Json}
import play.api.mvc.Results

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
case class BiscuitVerifierConfig(
    verifierRef: String = "",
    rbacPolicyRef: String = "",
    enableRemoteFacts: Boolean = false,
    remoteFactsRef: String = "",
    enforce: Boolean = true,
    extractorType: String = "header",
    extractorName: String = "Authorization"
) extends NgPluginConfig {
  def json: JsValue = BiscuitVerifierConfig.format.writes(this)
}

object BiscuitVerifierConfig {
  val configFlow: Seq[String] = Seq("verifier_ref", "rbac_ref", "enable_remote_facts", "remote_facts_ref", "enforce", "extractor_type", "extractor_name")
  def configSchema(name: String): Option[JsObject] = Some(Json.obj(
    "verifier_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"Biscuit Verifier",
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/${name}",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    ),
    "rbac_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"RBAC Policy Reference",
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-rbac",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
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
          Json.obj("label" -> "Header", "value" -> "header"),
          Json.obj("label" -> "Cookies", "value" -> "cookies"),
          Json.obj("label" -> "Query params", "value" -> "query")
        )
      ),
    ),
    "extractor_name" -> Json.obj(
      "type" -> "text",
      "label" -> "Biscuit field name"
    )
  ))

  val format = new Format[BiscuitVerifierConfig] {
    override def writes(o: BiscuitVerifierConfig): JsValue = Json.obj(
      "verifier_ref" -> o.verifierRef,
      "rbac_ref" -> o.rbacPolicyRef,
      "enable_remote_facts" -> o.enableRemoteFacts,
      "remote_facts_ref" -> o.remoteFactsRef,
      "enforce" -> o.enforce,
      "extractor_type" -> o.extractorType,
      "extractor_name" -> o.extractorName,
    )
    override def reads(json: JsValue): JsResult[BiscuitVerifierConfig] = Try {
      BiscuitVerifierConfig(
        verifierRef = json.select("verifier_ref").asOpt[String].getOrElse(""),
        rbacPolicyRef = json.select("rbac_ref").asOpt[String].getOrElse(""),
        enableRemoteFacts = json.select("enable_remote_facts").asOpt[Boolean].getOrElse(false),
        remoteFactsRef = json.select("remote_facts_ref").asOpt[String].getOrElse(""),
        enforce = json.select("enforce").asOpt[Boolean].getOrElse(true),
        extractorType = json.select("extractor_type").asOpt[String].getOrElse(""),
        extractorName = json.select("extractor_name").asOpt[String].getOrElse("")
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }
}
class BiscuitTokenValidator extends NgAccessValidator {

  private val logger = Logger("biscuit-token-validator-plugin")

  override def name: String = "Cloud APIM - Biscuit Tokens Verifier"

  override def description: Option[String] = "This plugin validates a Biscuit token".some

  override def defaultConfigObject: Option[NgPluginConfig] = Some(BiscuitVerifierConfig())

  override def core: Boolean = false

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = BiscuitVerifierConfig.configFlow

  override def configSchema: Option[JsObject] = BiscuitVerifierConfig.configSchema("biscuit-verifiers")

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand

  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Studio"), NgPluginCategory.AccessControl)

  override def steps: Seq[NgStep] = Seq(NgStep.ValidateAccess)

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Biscuit - token validator' plugin is available !")
    }
    ().vfuture
  }

  override def access(ctx: NgAccessContext)(implicit env: Env, ec: ExecutionContext): Future[NgAccess] = {
    val config = ctx.cachedConfig(internalName)(BiscuitVerifierConfig.format).getOrElse(BiscuitVerifierConfig())

    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitVerifier(config.verifierRef)) match {
      case None => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> "verifierRef not found"))).vfuture
      case Some(biscuitVerifier) => {
        env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(biscuitVerifier.keypairRef)) match {
          case None => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> "keypairRef not found"))).vfuture
          case Some(keypair) => {
            val publicKey = new PublicKey(biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519, keypair.pubKey)
            biscuitVerifier.config match {
              case Some(verifierConfig) => {
                if (config.rbacPolicyRef.nonEmpty) {
                  env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitRbacPolicy(config.rbacPolicyRef)) match {
                    case None => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> "rbacPolicyRef not found"))).vfuture
                    case Some(rbacPolicyConf) => {

                      val rbacConf = rbacPolicyConf.roles
                        .map(r => s"""role("${r._1}", ${r._2})""")
                        .map(_.stripSuffix(";"))
                        .toSeq

                      if (config.enableRemoteFacts && config.remoteFactsRef.nonEmpty) {
                        env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitRemoteFactsLoader(config.remoteFactsRef)) match {
                          case None => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> "remoteFactsRef not found"))).vfuture
                          case Some(remoteFactsEntity) => {
                            if (remoteFactsEntity.config.apiUrl.nonEmpty && remoteFactsEntity.config.headers.nonEmpty) {
                              BiscuitRemoteUtils.getRemoteFacts(remoteFactsEntity.config, ctx.json.asObject ++ Json.obj("phase" -> "access", "plugin" -> "biscuit_verifier")).flatMap {
                                case Left(error) => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Unable to get remote facts: ${error}"))).vfuture
                                case Right(factsData) => {
                                  val finalListFacts = verifierConfig.facts ++ factsData.roles ++ rbacConf ++ factsData.facts ++ factsData.acl
                                  val finalListOfRevokedId = verifierConfig.revokedIds ++ factsData.revoked
                                  val finalListOfChecks = verifierConfig.checks ++ factsData.checks

                                  BiscuitUtils.extractToken(ctx.request, config.extractorType, config.extractorName) match {
                                    case Some(token) => {
                                      Try(Biscuit.from_b64url(token, publicKey)).toEither match {
                                        case Left(err) => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Unable to deserialize Biscuit token : ${err}"))).vfuture
                                        case Right(biscuitToken) =>

                                          val verifierWithRemoteFacts = verifierConfig.copy(
                                            facts = finalListFacts,
                                            revokedIds = finalListOfRevokedId,
                                            checks = finalListOfChecks
                                          )

                                          BiscuitUtils.verify(biscuitToken, verifierWithRemoteFacts, AccessValidatorContext(ctx).some) match {
                                            case Left(err) => forbidden(ctx)
                                            case Right(_) => NgAccess.NgAllowed.vfuture
                                          }
                                      }
                                    }
                                    case None if config.enforce => forbidden(ctx)
                                    case None if !config.enforce => NgAccess.NgAllowed.vfuture
                                  }
                                }
                              }
                            } else {
                              NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Bad config for remoteFactsEntity"))).vfuture
                            }
                          }
                        }
                      } else {
                        BiscuitUtils.extractToken(ctx.request, config.extractorType, config.extractorName) match {
                          case Some(token) => {
                            Try(Biscuit.from_b64url(token, publicKey)).toEither match {
                              case Left(err) => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Unable to deserialize Biscuit token : ${err}"))).vfuture
                              case Right(biscuitUnverified) =>

                                Try(biscuitUnverified.verify(publicKey)).toEither match {
                                  case Left(err) => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Biscuit token is not valid : ${err}"))).vfuture
                                  case Right(biscuitToken) => {
                                    BiscuitUtils.verify(biscuitToken, verifierConfig.copy(facts = verifierConfig.facts ++ rbacConf), AccessValidatorContext(ctx).some) match {
                                      case Left(err) => forbidden(ctx)
                                      case Right(_) => NgAccess.NgAllowed.vfuture
                                    }
                                  }
                                }
                            }
                          }
                          case None if config.enforce => forbidden(ctx)
                          case None if !config.enforce => NgAccess.NgAllowed.vfuture
                        }
                      }


                    }
                  }
                } else {
                  BiscuitUtils.extractToken(ctx.request, config.extractorType, config.extractorName) match {
                    case Some(token) => {
                      Try(Biscuit.from_b64url(token, publicKey)).toEither match {
                        case Left(err) => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Unable to deserialize Biscuit token : ${err}"))).vfuture
                        case Right(biscuitUnverified) =>
                          Try(biscuitUnverified.verify(publicKey)).toEither match {
                            case Left(err) => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Biscuit token is not valid : ${err}"))).vfuture
                            case Right(biscuitToken) => {
                              BiscuitUtils.verify(biscuitToken, verifierConfig, AccessValidatorContext(ctx).some) match {
                                case Left(err) => forbidden(ctx)
                                case Right(_) => NgAccess.NgAllowed.vfuture
                              }
                            }
                          }
                      }
                    }
                    case None if config.enforce => forbidden(ctx)
                    case None if !config.enforce => NgAccess.NgAllowed.vfuture
                  }
                }
              }
              case None => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Bad biscuit verifier configuration"))).vfuture
            }
          }
        }
      }
    }
  }

  def forbidden(ctx: NgAccessContext)(implicit env: Env, ec: ExecutionContext): Future[NgAccess] = {
    Errors
      .craftResponseResult(
        "forbidden",
        Results.Forbidden,
        ctx.request,
        None,
        None,
        duration = ctx.report.getDurationNow(),
        overhead = ctx.report.getOverheadInNow(),
        attrs = ctx.attrs,
        maybeRoute = ctx.route.some
      )
      .map(r => NgAccess.NgDenied(r))
  }
}
