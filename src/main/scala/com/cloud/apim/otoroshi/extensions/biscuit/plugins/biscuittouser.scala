package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins

import akka.Done
import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitExtractorConfig, VerificationContext}
import org.biscuitsec.biscuit.datalog.{RunLimits, SymbolTable}
import org.biscuitsec.biscuit.token.builder.Term.Str
import org.biscuitsec.biscuit.token.{Biscuit, UnverifiedBiscuit}
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
  userIdKey: String = "user_id",
  nameKey: String = "name",
  emailKey: String = "email",
  validations: JsObject = Json.obj(),
  verifierRef: Option[String] = None,
) extends NgPluginConfig {
  def json: JsValue = BiscuitUserExtractorConfig.format.writes(this)
}

object BiscuitUserExtractorConfig {
  val configFlow: Seq[String] = Seq("keypair_ref", "enforce", "extractor_type", "extractor_name", "email_key", "name_key", "user_id_key", "verifier_ref", "validations")
  val format = new Format[BiscuitUserExtractorConfig] {
    override def writes(o: BiscuitUserExtractorConfig): JsValue = Json.obj(
      "keypair_ref" -> o.keypairRef,
      "enforce" -> o.enforce,
      "extractor_type" -> o.extractorType,
      "extractor_name" -> o.extractorName,
      "email_key" -> o.emailKey,
      "name_key" -> o.nameKey,
      "user_id_key" -> o.userIdKey,
      "validations" -> o.validations,
      "verifier_ref" -> o.verifierRef,
    )

    override def reads(json: JsValue): JsResult[BiscuitUserExtractorConfig] = Try {
      BiscuitUserExtractorConfig(
        keypairRef = json.select("keypair_ref").asOpt[String].getOrElse(""),
        enforce = json.select("enforce").asOpt[Boolean].getOrElse(true),
        extractorType = json.select("extractor_type").asOpt[String].getOrElse(""),
        extractorName = json.select("extractor_name").asOpt[String].getOrElse(""),
        emailKey = json.select("email_key").asOpt[String].orElse(json.select("username_key").asOpt[String]).getOrElse("email"),
        nameKey = json.select("name_key").asOpt[String].getOrElse("name"),
        userIdKey = json.select("user_id_key").asOpt[String].getOrElse("user"),
        validations = json.select("validations").asOpt[JsObject].getOrElse(Json.obj()),
        verifierRef = json.select("verifier_ref").asOpt[String],
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
      "type" -> "string",
      "label" -> "Biscuit field name"
    ),
    "email_key" -> Json.obj(
      "type" -> "string",
      "label" -> "User email biscuit key"
    ),
    "name_key" -> Json.obj(
      "type" -> "string",
      "label" -> "User name biscuit key"
    ),
    "user_id_key" -> Json.obj(
      "type" -> "string",
      "label" -> "User ID biscuit key"
    ),
    "validations" -> Json.obj(
      "type" -> "json",
      "label" -> "Additional biscuit validations",
      "props" -> Json.obj(
        "editorOnly" -> false,
      )
    ),
    "verifier_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"Biscuit Verifier",
      "placeholder" -> "Optional biscuit verifier",
      "props" -> Json.obj(
        "placeholder" -> "Optional biscuit verifier",
        "isClearable" -> true,
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-verifiers",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
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

  override def steps: Seq[NgStep] = Seq(NgStep.PreRoute)

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
    val ext = env.adminExtensions.extension[BiscuitExtension].get
    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(config.keypairRef)) match {
      case None => handleError("keypair_ref not found")
      case Some(keypair) => {
        BiscuitExtractorConfig(config.extractorType, config.extractorName).extractToken(ctx.request, None, ctx.attrs) match {
          case Some(token) => {
            Try(Biscuit.from_b64url(token, keypair.getPubKey)).toEither match {
              case Left(err) => handleError(s"Unable to deserialize Biscuit token : ${err}")
              case Right(biscuitUnverified) => {
                Try(biscuitUnverified.verify(keypair.getPubKey)).toEither match {
                  case Left(err) => handleError(s"Biscuit token is not valid : ${err}")
                  case Right(biscuitToken) => {
                    config.verifierRef
                      .map(ref => ext.datastores.biscuitVerifierDataStore.findById(ref))
                      .getOrElse(None.vfuture) flatMap {
                        case Some(verifier) => verifier.verify(ctx.request, Some(VerificationContext(ctx.route, ctx.request, None, None, ctx.attrs)), ctx.attrs).flatMap {
                          case Left(err) => handleError(s"invalid biscuit token: ${err}")
                          case Right(_) => extractIdNameAndEmail(ctx, biscuitToken, config)
                        }
                        case None => {
                          val facts = config.validations.select("facts").asOpt[Seq[String]].getOrElse(Seq.empty[String])
                          val rules = config.validations.select("rules").asOpt[Seq[String]].getOrElse(Seq.empty[String])
                          val checks = config.validations.select("checks").asOpt[Seq[String]].getOrElse(Seq.empty[String])
                          val policies = config.validations.select("policies").asOpt[Seq[String]].getOrElse(Seq.empty[String])
                          if (facts.isEmpty && rules.isEmpty && checks.isEmpty && policies.isEmpty) {
                            extractIdNameAndEmail(ctx, biscuitToken, config)
                          } else {
                            val authorizer = biscuitUnverified.authorizer()
                            authorizer.set_time()
                            val maxFacts = ext.configuration.getOptional[Int]("verifier_run_limit.max_facts").getOrElse(1000)
                            val maxIterations = ext.configuration.getOptional[Int]("verifier_run_limit.max_iterations").getOrElse(100)
                            val maxTime = java.time.Duration.ofMillis(ext.configuration.getOptional[Long]("verifier_run_limit.max_time").getOrElse(1000))
                            facts.foreach(str => authorizer.add_fact(str))
                            rules.foreach(str => authorizer.add_rule(str))
                            checks.foreach(str => authorizer.add_check(str))
                            policies.foreach(str => authorizer.add_policy(str))
                            Try(authorizer.authorize(new RunLimits(maxFacts, maxIterations, maxTime))).toEither match {
                              case Left(err) => handleError(s"invalid biscuit token: ${err}")
                              case Right(_) => extractIdNameAndEmail(ctx, biscuitToken, config)
                            }
                          }
                        }
                      }
                  }
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
            val cleanName = name.get()
            finalProfile.value.get(cleanName) match {
              case None => finalProfile = finalProfile + (name.get() -> JsString(cleanValue))
              case Some(JsString(str)) => finalProfile = finalProfile + (name.get() -> Json.arr(str, JsString(cleanValue)))
              case Some(JsArray(arr)) => finalProfile = finalProfile + (name.get() -> JsArray(arr :+ JsString(cleanValue)))
              case Some(jsv) => finalProfile = finalProfile + (name.get() -> Json.arr(jsv, JsString(cleanValue)))
            }
          }
        }
      }
    finalProfile
  }

  def extractIdNameAndEmail(ctx: NgPreRoutingContext, biscuitToken: Biscuit, config: BiscuitUserExtractorConfig)(implicit env: Env, ec: ExecutionContext): Future[Either[NgPreRoutingError, Done]] = {
    val otoroshiEmail = biscuitToken.authorizer().query(s"biscuit_email($$id) <- ${config.emailKey}($$id)")
    val otoroshiName = biscuitToken.authorizer().query(s"biscuit_name($$id) <- ${config.nameKey}($$id)")
    val otoroshiUserId = biscuitToken.authorizer().query(s"biscuit_user_id($$id) <- ${config.userIdKey}($$id)")

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

    val biscuitEmail: Option[String] = {
      Try(otoroshiEmail).toOption
        .map(_.asScala)
        .flatMap(_.headOption)
        .filter(_.name() == "biscuit_email")
        .map(_.terms().asScala)
        .flatMap(_.headOption)
        .flatMap {
          case str: Str => str.getValue.some
          case _ => None
        }
    }

    val biscuitName: Option[String] = {
      Try(otoroshiName).toOption
        .map(_.asScala)
        .flatMap(_.headOption)
        .filter(_.name() == "biscuit_name")
        .map(_.terms().asScala)
        .flatMap(_.headOption)
        .flatMap {
          case str: Str => str.getValue.some
          case _ => None
        }
      }


    val finalProfile = extractBiscuitTokenInfo(biscuitToken)
    (biscuitUserId, biscuitEmail, biscuitName) match {
      case (userId, Some(userEmail), userName) => {
        val user: PrivateAppsUser = PrivateAppsUser(
          randomId = userId.getOrElse(IdGenerator.uuid),
          name = userName.getOrElse(userEmail),
          email = userEmail,
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

  def handleError(message: String): Future[Either[NgPreRoutingError, Done]] = {
    logger.error(message)
    NgPreRoutingErrorWithResult(Results.Unauthorized(Json.obj("error" -> "Biscuit token is not valid"))).leftf
  }
}