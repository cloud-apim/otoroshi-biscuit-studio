package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitVerifierConfig
import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitUtils, PubKeyBiscuitToken}
import org.biscuitsec.biscuit.crypto.PublicKey
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.env.Env
import otoroshi.gateway.Errors
import otoroshi.next.plugins.AccessValidatorContext
import otoroshi.next.plugins.api.{NgAccess, NgAccessContext, NgAccessValidator, NgPluginCategory, NgPluginConfig, NgPluginVisibility, NgStep}
import otoroshi.utils.syntax.implicits.BetterSyntax
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.mvc.Results
import play.api.Logger
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BiscuitTokenValidator extends NgAccessValidator {

  override def name: String                                = "Cloud APIM - Biscuit Tokens Verifier"
  override def description: Option[String]                 = "This plugin validates a Biscuit token".some
  override def defaultConfigObject: Option[NgPluginConfig] = Some(BiscuitVerifierConfig.default)
  override def core: Boolean                               = false
  override def noJsForm: Boolean = true
  override def configFlow: Seq[String] = BiscuitVerifierConfig.configFlow
  override def configSchema: Option[JsObject] = BiscuitVerifierConfig.configSchema("biscuit-verifiers")
  override def visibility: NgPluginVisibility              = NgPluginVisibility.NgUserLand
  override def categories: Seq[NgPluginCategory]           = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Tokens"), NgPluginCategory.AccessControl)
  override def steps: Seq[NgStep]                          = Seq(NgStep.ValidateAccess)

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Biscuit - token validator' plugin is available !")
    }
    ().vfuture
  }

  private val logger = Logger("biscuit-token-validator-plugin")

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
                BiscuitUtils.extractToken(ctx.request, config.extractorType, config.extractorName) match {
                  case Some(PubKeyBiscuitToken(token)) => {

                    Try(Biscuit.from_b64url(token, publicKey)).toEither match {
                      case Left(err) => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Unable to deserialize Biscuit token : ${err}"))).vfuture
                      case Right(biscuitUnverified) =>

                        Try(biscuitUnverified.verify(publicKey)).toEither match {
                          case Left(err) =>  NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Biscuit token is not valid : ${err}"))).vfuture
                          case Right(biscuitToken) => {

                            BiscuitUtils.verify(biscuitToken, verifierConfig, AccessValidatorContext(ctx)) match {
                              case Left(_)  => {
                                forbidden(ctx)
                              }
                              case Right(_) => NgAccess.NgAllowed.vfuture
                            }
                          }
                        }
                    }
                  }
                  case None if config.enforce => forbidden(ctx)
                  case None if !config.enforce =>  NgAccess.NgAllowed.vfuture
                }
              }
              case None =>  NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> s"Bad biscuit verifier configuration"))).vfuture
            }
          }
        }
      }
    }
  }
}
