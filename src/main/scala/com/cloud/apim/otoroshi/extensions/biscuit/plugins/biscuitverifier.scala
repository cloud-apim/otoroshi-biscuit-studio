package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitVerifierConfig
import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitRemoteUtils, BiscuitUtils}
import org.biscuitsec.biscuit.crypto.PublicKey
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.env.Env
import otoroshi.gateway.Errors
import otoroshi.next.plugins.AccessValidatorContext
import otoroshi.next.plugins.api._
import otoroshi.utils.syntax.implicits.{BetterJsReadable, BetterSyntax}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BiscuitTokenValidator extends NgAccessValidator {

  private val logger = Logger("biscuit-token-validator-plugin")

  override def name: String = "Cloud APIM - Biscuit Tokens Verifier"

  override def description: Option[String] = "This plugin validates a Biscuit token".some

  override def defaultConfigObject: Option[NgPluginConfig] = Some(BiscuitVerifierConfig.default)

  override def core: Boolean = false

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = BiscuitVerifierConfig.configFlow

  override def configSchema: Option[JsObject] = BiscuitVerifierConfig.configSchema("biscuit-verifiers")

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand

  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Tokens"), NgPluginCategory.AccessControl)

  override def steps: Seq[NgStep] = Seq(NgStep.ValidateAccess)

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Biscuit - token validator' plugin is available !")
    }
    ().vfuture
  }

  override def access(ctx: NgAccessContext)(implicit env: Env, ec: ExecutionContext): Future[NgAccess] = {
    val config = ctx.cachedConfig(internalName)(BiscuitVerifierConfig.format).getOrElse(BiscuitVerifierConfig.default)

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

                                  BiscuitUtils.extractToken(ctx.request, config.extractorType, config.extractorName) match {
                                    case Some(token) => {
                                      Try(Biscuit.from_b64url(token, publicKey)).toEither match {
                                        case Left(err) => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Unable to deserialize Biscuit token : ${err}"))).vfuture
                                        case Right(biscuitUnverified) =>

                                          Try(biscuitUnverified.verify(publicKey)).toEither match {
                                            case Left(err) => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Biscuit token is not valid : ${err}"))).vfuture
                                            case Right(biscuitToken) => {

                                              BiscuitUtils.verify(biscuitToken, verifierConfig.copy(facts = finalListFacts, revokedIds = finalListOfRevokedId), AccessValidatorContext(ctx).some) match {
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
