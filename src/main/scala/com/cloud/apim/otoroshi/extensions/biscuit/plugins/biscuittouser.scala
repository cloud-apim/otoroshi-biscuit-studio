package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.Done
import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitExtractorConfig
import org.biscuitsec.biscuit.datalog.SymbolTable
import org.biscuitsec.biscuit.token.{Biscuit, UnverifiedBiscuit}
import org.biscuitsec.biscuit.token.builder.Term.Str
import org.joda.time.DateTime
import otoroshi.env.Env
import otoroshi.models.PrivateAppsUser
import otoroshi.next.plugins.api._
import otoroshi.security.IdGenerator
import otoroshi.utils.syntax.implicits.{BetterJsValue, BetterSyntax}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Results

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.{asScalaBufferConverter, asScalaSetConverter, collectionAsScalaIterableConverter}
import scala.util.{Failure, Success, Try}

case class BiscuitUserExtractorConfig(
  keypairRef: String = "",
  enforce: Boolean = true,
  extractorType: String = "header",
  extractorName: String = "Authorization",
  usernameKey: String = "name"
) extends NgPluginConfig {
  def json: JsValue = BiscuitUserExtractorConfig.format.writes(this)
}

object BiscuitUserExtractorConfig {
  val configFlow: Seq[String] = Seq("keypair_ref", "enforce", "extractor_type", "extractor_name", "username_key")
  val format = new Format[BiscuitUserExtractorConfig] {
    override def writes(o: BiscuitUserExtractorConfig): JsValue = Json.obj(
      "keypair_ref" -> o.keypairRef,
      "enforce" -> o.enforce,
      "extractor_type" -> o.extractorType,
      "extractor_name" -> o.extractorName,
      "username_key" -> o.usernameKey
    )

    override def reads(json: JsValue): JsResult[BiscuitUserExtractorConfig] = Try {
      BiscuitUserExtractorConfig(
        keypairRef = json.select("keypair_ref").asOpt[String].getOrElse(""),
        enforce = json.select("enforce").asOpt[Boolean].getOrElse(true),
        extractorType = json.select("extractor_type").asOpt[String].getOrElse(""),
        extractorName = json.select("extractor_name").asOpt[String].getOrElse(""),
        usernameKey = json.select("username_key").asOpt[String].getOrElse("")
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }

  def configSchema: Option[JsObject] = Some(Json.obj(
    "keypair_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"Biscuit Keypair Reference",
      "props" -> Json.obj(
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs",
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
          Json.obj("label" -> "Cookies", "value" -> "cookie"),
          Json.obj("label" -> "Query params", "value" -> "query")
        )
      ),
    ),
    "extractor_name" -> Json.obj(
      "type" -> "text",
      "label" -> "Biscuit field name"
    ),
    "username_key" -> Json.obj(
      "type" -> "text",
      "label" -> "Username biscuit key"
    )
  ))
}

class BiscuitUserExtractor extends NgPreRouting {

  private val logger = Logger("biscuit-user-extractor-plugin")

  override def name: String = "Cloud APIM - Biscuit User Extractor"

  override def description: Option[String] = "This plugin extract an user from a biscuit token".some

  override def defaultConfigObject: Option[NgPluginConfig] = Some(BiscuitUserExtractorConfig())

  override def core: Boolean = false

  override def noJsForm: Boolean = true

  override def configFlow: Seq[String] = BiscuitUserExtractorConfig.configFlow

  override def configSchema: Option[JsObject] = BiscuitUserExtractorConfig.configSchema

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand

  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Cloud APIM"), NgPluginCategory.Custom("Biscuit Studio"), NgPluginCategory.AccessControl)

  override def steps: Seq[NgStep] = Seq(NgStep.ValidateAccess)

  override def start(env: Env): Future[Unit] = {
    env.adminExtensions.extension[BiscuitExtension].foreach { ext =>
      ext.logger.info("the 'Cloud APIM - Biscuit User Extractor' plugin is available !")
    }
    ().vfuture
  }

  override def preRoute(
    ctx: NgPreRoutingContext
  )(implicit env: Env, ec: ExecutionContext): Future[Either[NgPreRoutingError, Done]] = {

    val config = ctx.cachedConfig(internalName)(BiscuitUserExtractorConfig.format).getOrElse(BiscuitUserExtractorConfig())
    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(config.keypairRef)) match {
      case None => handleError("keypair_ref not found")
      case Some(keypair) => {
        BiscuitExtractorConfig(config.extractorType, config.extractorName).extractToken(ctx.request, None) match {
          case Some(token) => {
            Try(Biscuit.from_b64url(token, keypair.getPubKey)).toEither match {
              case Left(err) => handleError(s"Unable to deserialize Biscuit token : ${err}")
              case Right(biscuitUnverified) =>

                Try(biscuitUnverified.verify(keypair.getPubKey)).toEither match {
                  case Left(err) => handleError(s"Biscuit token is not valid : ${err}")
                  case Right(biscuitToken) => {
                    extractIdAndName(ctx, biscuitToken, config)
                  }
                }
            }
          }
          case None if config.enforce => unauthorized(Json.obj("error" -> "unauthorized", "error_description" -> "Biscuit not found in request"))
          case None if !config.enforce => Done.right.vfuture
        }
      }
    }
  }

  def extractBiscuitTokenInfo(biscuitToken: Biscuit): JsObject = {
    var finalProfile = Json.obj()

    val clazz = classOf[UnverifiedBiscuit]
    Try(clazz.getField("symbols"))
      .orElse(Try(clazz.getDeclaredField("symbols")))
      .toOption
      .map { f =>
        f.setAccessible(true)
        f
      }.flatMap(v => Option(v.get(biscuitToken))).map(_.asInstanceOf[SymbolTable]).foreach { symbols =>
        biscuitToken.authorizer().facts().facts().values().asScala.flatMap(_.asScala).foreach { fact =>
          val nameLng = fact.predicate().name().toInt
          val name = symbols.get_s(nameLng)
          val value: String = fact.predicate().terms().asScala.map(t => symbols.print_term(t)).mkString(", ")
          if (name.isDefined) {
            val cleanValue = value.replaceAll("^\"|\"$", "").replace("\\\"", "\"")
            finalProfile = finalProfile + (name.get() -> JsString(cleanValue))
          }
        }
      }
    finalProfile
  }

  def extractIdAndName(ctx: NgPreRoutingContext, biscuitToken: Biscuit, config: BiscuitUserExtractorConfig)(implicit env: Env, ec: ExecutionContext): Future[Either[NgPreRoutingError, Done]] = {
    val otoroshiUserId = biscuitToken.authorizer().query("biscuit_user_id($id) <- user_id($id)")
    val otoroshiUsername = biscuitToken.authorizer().query(s"biscuit_username($$id) <- ${config.usernameKey}($$id)")

    val biscuitUserId: Option[String] = {
      Try(otoroshiUserId).toOption
        .map(_.asScala)
        .flatMap(_.headOption)
        .filter(_.name() == "biscuit_user_id")
        .map(_.terms().asScala)
        .flatMap(_.headOption)
        .flatMap {
          case str: Str => str.getValue.some
          case _ => None
        }
    }

    val biscuitUsername: Option[String] = {
      Try(otoroshiUsername).toOption
        .map(_.asScala)
        .flatMap(_.headOption)
        .filter(_.name() == "biscuit_username")
        .map(_.terms().asScala)
        .flatMap(_.headOption)
        .flatMap {
          case str: Str => str.getValue.some
          case _ => None
        }
    }


    val finalProfile = extractBiscuitTokenInfo(biscuitToken)
    (biscuitUserId, biscuitUsername) match {
      case (Some(userId), Some(userName)) => {

        val user: PrivateAppsUser = PrivateAppsUser(
          randomId = IdGenerator.uuid,
          name = userId,
          email = userName,
          otoroshiData = None,
          profile = finalProfile,
          token = Json.obj("biscuit" -> biscuitToken.serialize_b64url()),
          realm = s"BiscuitUserExtractor@${ctx.route.serviceDescriptor.id}",
          authConfigId = s"BiscuitUserExtractor@${ctx.route.serviceDescriptor.id}",
          createdAt = DateTime.now(),
          expiredAt = DateTime.now().plusHours(1),
          lastRefresh = DateTime.now(),
          tags = Seq.empty,
          metadata = Map.empty,
          location = ctx.route.serviceDescriptor.location
        )
        ctx.attrs.put(otoroshi.plugins.Keys.UserKey -> user)

        Done.right.vfuture
      }
      case _ => unauthorized(Json.obj("error" -> "unauthorized", "error_description" -> "Bad user extraction, user id or username not valid"))
    }
  }

  def unauthorized(error: JsObject): Future[Either[NgPreRoutingError, Done]] = {
    NgPreRoutingErrorWithResult(Results.Unauthorized(error)).leftf
  }

  def handleError(message: String): Future[Either[NgPreRoutingError, Done]] =
    NgPreRoutingErrorWithResult(Results.InternalServerError(Json.obj("error" -> message))).leftf
}