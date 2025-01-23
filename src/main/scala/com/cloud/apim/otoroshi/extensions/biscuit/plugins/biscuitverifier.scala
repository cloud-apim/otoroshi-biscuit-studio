package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitVerifierConfig
import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitRemoteUtils, BiscuitUtils}
import org.biscuitsec.biscuit.crypto.PublicKey
import org.biscuitsec.biscuit.token.Biscuit
import org.biscuitsec.biscuit.token.builder.Term.Str
import otoroshi.env.Env
import otoroshi.gateway.Errors
import otoroshi.next.plugins.AccessValidatorContext
import otoroshi.next.plugins.api._
import otoroshi.utils.syntax.implicits.BetterSyntax
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.{asScalaBufferConverter, asScalaSetConverter}
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

  def handleError(message: String): Future[NgAccess] =
    NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> message))).vfuture


  override def access(ctx: NgAccessContext)(implicit env: Env, ec: ExecutionContext): Future[NgAccess] = {
    val config = ctx.cachedConfig(internalName)(BiscuitVerifierConfig.format).getOrElse(BiscuitVerifierConfig.default)

    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitVerifier(config.verifierRef)) match {
      case None => handleError("verifierRef not found")
      case Some(biscuitVerifier) => {
        env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(biscuitVerifier.keypairRef)) match {
          case None => handleError("keypairRef not found")
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
                          case None => handleError("remoteFactsRef not found")
                          case Some(remoteFactsEntity) => {
                            if (remoteFactsEntity.config.nonEmpty && remoteFactsEntity.config.get.apiUrl.nonEmpty && remoteFactsEntity.config.get.headers.nonEmpty) {
                              BiscuitRemoteUtils.getRemoteFacts(remoteFactsEntity.config.get.apiUrl, remoteFactsEntity.config.get.headers).flatMap {
                                case Left(error) => handleError(s"Unable to get remote facts: ${error}")
                                case Right(listFacts) => {
                                  val rolesRemotes = listFacts._1
                                  val remoteRevokedIt = listFacts._2
                                  val remoteFacts = listFacts._3
                                  val aclRemote = listFacts._4
                                  val finalListFacts = verifierConfig.facts ++ rolesRemotes ++ rbacConf ++ remoteFacts ++ aclRemote
                                  val finalListOfRevokedId = verifierConfig.revokedIds ++ remoteRevokedIt

                                  BiscuitUtils.extractToken(ctx.request, config.extractorType, config.extractorName) match {
                                    case Some(token) => {
                                      Try(Biscuit.from_b64url(token, publicKey)).toEither match {
                                        case Left(err) => handleError(s"Unable to deserialize Biscuit token : ${err}")
                                        case Right(biscuitUnverified) =>

                                          Try(biscuitUnverified.verify(publicKey)).toEither match {
                                            case Left(err) => handleError(s"Biscuit token is not valid : ${err}")
                                            case Right(biscuitToken) => {

                                              BiscuitUtils.verify(biscuitToken, verifierConfig.copy(facts = finalListFacts, revokedIds = finalListOfRevokedId), AccessValidatorContext(ctx).some) match {
                                                case Left(err) => forbidden(ctx)
                                                case Right(_) => {
                                                  extractApiKey(ctx, biscuitToken)
                                                }
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
                              handleError(s"Bad config for remoteFactsEntity")
                            }
                          }
                        }
                      } else {
                        BiscuitUtils.extractToken(ctx.request, config.extractorType, config.extractorName) match {
                          case Some(token) => {
                            Try(Biscuit.from_b64url(token, publicKey)).toEither match {
                              case Left(err) => handleError(s"Unable to deserialize Biscuit token : ${err}")
                              case Right(biscuitUnverified) =>

                                Try(biscuitUnverified.verify(publicKey)).toEither match {
                                  case Left(err) => handleError(s"Biscuit token is not valid : ${err}")
                                  case Right(biscuitToken) => {
                                    BiscuitUtils.verify(biscuitToken, verifierConfig.copy(facts = verifierConfig.facts ++ rbacConf), AccessValidatorContext(ctx).some) match {
                                      case Left(err) => forbidden(ctx)
                                      case Right(_) => {
                                        extractApiKey(ctx, biscuitToken)
                                      }
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
                        case Left(err) => handleError(s"Unable to deserialize Biscuit token : ${err}")
                        case Right(biscuitUnverified) =>
                          Try(biscuitUnverified.verify(publicKey)).toEither match {
                            case Left(err) => handleError(s"Biscuit token is not valid : ${err}")
                            case Right(biscuitToken) => {
                              BiscuitUtils.verify(biscuitToken, verifierConfig, AccessValidatorContext(ctx).some) match {
                                case Left(err) => forbidden(ctx)
                                case Right(_) => {
                                  extractApiKey(ctx, biscuitToken)
                                }
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
              case None => handleError(s"Bad biscuit verifier configuration")
            }
          }
        }
      }
    }
  }

  def extractApiKey(ctx: NgAccessContext, biscuitToken: Biscuit)(implicit env: Env, ec: ExecutionContext): Future[NgAccess] = {
    val otoroshiClientID = biscuitToken.authorizer().query("api_key_client_id($id) <- otoroshi_client_id($id)")

    val client_id: Option[String]   = Try(otoroshiClientID).toOption
      .map(_.asScala)
      .flatMap(_.headOption)
      .filter(_.name() == "api_key_client_id")
      .map(_.terms().asScala)
      .flatMap(_.headOption)
      .flatMap {
        case str: Str => str.getValue().some
        case _        => None
      }

    if(client_id.isDefined){
      env.datastores.apiKeyDataStore.findById(client_id.get).flatMap {
        case Some(apikey) => {
          ctx.attrs.put(otoroshi.plugins.Keys.ApiKeyKey -> apikey)

          NgAccess.NgAllowed.vfuture
        }
        case _ => handleError(s"bad apikey - not found in biscuit token")
      }
    }else{
      NgAccess.NgAllowed.vfuture
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
