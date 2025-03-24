package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitVerifier, VerificationContext}
import otoroshi.env.Env
import otoroshi.gateway.Errors
import otoroshi.next.plugins.api.NgAccess.NgAllowed
import otoroshi.next.plugins.api._
import otoroshi.utils.syntax.implicits.{BetterJsValue, BetterSyntax}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Results

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class BiscuitVerifierPluginConfig(
  verifierRefs: Seq[String] = Seq.empty,
  enforce: Boolean = true,
) extends NgPluginConfig {
  def json: JsValue = BiscuitVerifierPluginConfig.format.writes(this)
}

object BiscuitVerifierPluginConfig {

  val configFlow: Seq[String] = Seq("verifier_refs", "enforce")
  val format = new Format[BiscuitVerifierPluginConfig] {
    override def writes(o: BiscuitVerifierPluginConfig): JsValue = Json.obj(
      "verifier_refs" -> o.verifierRefs,
      "enforce" -> o.enforce,
    )

    override def reads(json: JsValue): JsResult[BiscuitVerifierPluginConfig] = Try {
      BiscuitVerifierPluginConfig(
        verifierRefs = json.select("verifier_refs").asOpt[Seq[String]].getOrElse(Seq.empty) ++ json.select("verifier_ref").asOpt[String],
        enforce = json.select("enforce").asOpt[Boolean].getOrElse(true)
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }

  def configSchema: Option[JsObject] = Some(Json.obj(
    "verifier_refs" -> Json.obj(
      "type" -> "select",
      "label" -> s"Biscuit Verifiers",
      "array" -> true,
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-verifiers",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    ),
    "enforce" -> Json.obj(
      "type" -> "bool",
      "label" -> "Enforce"
    )
  ))
}

class BiscuitTokenVerifierPlugin extends NgAccessValidator {

  private val logger = Logger("biscuit-verifier-plugin")

  override def name: String = "Cloud APIM - Biscuit Tokens Verifier"

  override def description: Option[String] = "This plugin validates a Biscuit token".some

  override def defaultConfigObject: Option[NgPluginConfig] = Some(BiscuitVerifierPluginConfig())

  override def core: Boolean = false

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = BiscuitVerifierPluginConfig.configFlow

  override def configSchema: Option[JsObject] = BiscuitVerifierPluginConfig.configSchema

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
    val config = ctx.cachedConfig(internalName)(BiscuitVerifierPluginConfig.format).getOrElse(BiscuitVerifierPluginConfig())

    env.adminExtensions.extension[BiscuitExtension] match {
      case None => NgAccess.NgDenied(Results.InternalServerError(Json.obj("error" -> "extension not found"))).vfuture
      case Some(ext) => {
        val verifiers = config.verifierRefs.flatMap(id => ext.states.biscuitVerifier(id))
        var hasFailed = false
        var errors = Seq.empty[String]

        def next(items: Seq[BiscuitVerifier]): Future[NgAccess] = {
          items.headOption match {
            case None if hasFailed => forbidden(ctx, errors)
            case None if !hasFailed && config.enforce => forbidden(ctx, errors)
            case None if !hasFailed && !config.enforce => NgAllowed.vfuture
            case Some(head) => {
              head.verify(ctx.request, Some(VerificationContext(ctx.route, ctx.request, ctx.user, ctx.apikey)), ctx.attrs).flatMap {
                case Left(err) if err == "no token" => {
                  errors = errors:+ err
                  next(items.tail)
                }
                case Left(err) =>{
                  hasFailed = true
                  errors = errors:+ err
                  next(items.tail)
                }
                case Right(_) => {
                  hasFailed = false
                  NgAllowed.vfuture
                }
              }
            }
          }
        }

        if (verifiers.isEmpty) {
          NgAccess.NgAllowed.vfuture
        } else {
          next(verifiers)
        }
      }
    }
  }

  def forbidden(ctx: NgAccessContext, msg: Seq[String] = Seq.empty)(implicit env: Env, ec: ExecutionContext): Future[NgAccess] = {
    Errors
      .craftResponseResult(
        msg.mkString(", "),
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
