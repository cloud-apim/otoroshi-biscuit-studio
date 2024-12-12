package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.stream.Materializer
import com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitAttenuatorConfig
import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitUtils, PubKeyBiscuitToken}
import org.biscuitsec.biscuit.crypto.PublicKey
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.env.Env
import otoroshi.next.plugins.api.{NgPluginCategory, NgPluginConfig, NgPluginHttpRequest, NgPluginVisibility, NgRequestTransformer, NgStep, NgTransformerRequestContext, NgTransformerResponseContext}
import otoroshi.utils.syntax.implicits.BetterSyntax
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.mvc.{Result, Results}
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.libs.ws.WSCookie

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BiscuitTokenAttenuator extends NgRequestTransformer {

  private val logger = Logger("biscuit-token-attenuator-plugin")

  override def description: Option[String] = "Plugin to attenuate a biscuit token".some

  override def core: Boolean = false

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand

  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Tokens"))

  override def steps: Seq[NgStep] = Seq(NgStep.TransformRequest)

  override def defaultConfigObject: Option[NgPluginConfig] = Some(BiscuitAttenuatorConfig.default)

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = BiscuitAttenuatorConfig.configFlow

  override def configSchema: Option[JsObject] = BiscuitAttenuatorConfig.configSchema("biscuit-attenuators")

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Biscuit Attenuator' plugin is available !")
    }
    ().vfuture
  }

  override def transformRequestSync(ctx: NgTransformerRequestContext)(implicit env: Env, ec: ExecutionContext, mat: Materializer): Either[Result, NgPluginHttpRequest] = {
    val config = ctx.cachedConfig(internalName)(BiscuitAttenuatorConfig.format).getOrElse(BiscuitAttenuatorConfig.default)

    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitAttenuator(config.ref)) match {
      case None =>  Left(Results.InternalServerError(Json.obj("error" -> "attenuator config not found")))
      case Some(attenuator) => {
        attenuator.config match {
          case None =>  Left(Results.InternalServerError(Json.obj("error" -> "bad attenuator config")))
          case Some(attenuatorConfig) => {
            env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(attenuator.keypairRef)) match {
              case None => Left(Results.InternalServerError(Json.obj("error" -> "keypair not existing")))
              case Some(keypair) => {
                val publicKey = new PublicKey(biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519, keypair.pubKey)

                BiscuitUtils.extractToken(ctx.request, config.extractorType, config.extractorName) match {
                  case None => Left(Results.InternalServerError(Json.obj("error" -> "token not found from header")))
                  case Some(PubKeyBiscuitToken(token)) => {
                    Try(Biscuit.from_b64url(token, publicKey)).toEither match {
                      case Left(errortoken) => Left(Results.InternalServerError(Json.obj("error" -> "unable to deserialize biscuit token")))
                      case Right(biscuitUnverified) =>
                        Try(biscuitUnverified.verify(publicKey)).toEither match {
                          case Left(_) => ctx.otoroshiRequest.right
                          case Right(biscuitToken) => {
                            val attenuatedToken = BiscuitUtils.attenuateToken(biscuitToken, attenuatorConfig.checks)

                            config.extractorType match {
                              case "header" => ctx.otoroshiRequest
                                .copy(
                                  headers = ctx.otoroshiRequest.headers ++ Map(config.extractorName -> s"biscuit:${attenuatedToken.serialize_b64url()}")
                                )
                                .right
                              case "query" => {
                                val uri      = ctx.otoroshiRequest.uri
                                val newQuery = uri.rawQueryString.map(_ => uri.query().filterNot(_._1 == config.extractorName).toString()).get ++ s"${config.extractorName}=biscuit:${attenuatedToken.serialize_b64url()}"
                                val newUrl   = uri.copy(rawQueryString = newQuery.some).toString()
                                ctx.otoroshiRequest.copy(url = newUrl).right
                              }
                              case "cookie" => {
                                val cookie = DefaultWSCookie(
                                  name = config.extractorName,
                                  value = s"biscuit:${attenuatedToken.serialize_b64url()}",
                                  maxAge = Some(360000),
                                  path = "/".some,
                                  domain = ctx.request.domain.some,
                                  httpOnly = false
                                )
                                ctx.otoroshiRequest
                                  .copy(
                                    cookies = ctx.otoroshiRequest.cookies ++ Seq(cookie)
                                  )
                                  .right
                              }
                              case _ => Left(Results.InternalServerError(Json.obj("error" -> "bad extractor config")))
                            }
                          }
                        }
                    }
                  }
                }
              }
              case None => ctx.otoroshiRequest.right
            }

          }
        }

      }
    }
  }

  override def name: String = "Cloud APIM - Biscuit Tokens Attenuator"
}