package com.cloud.apim.otoroshi.extensions.biscuit.entities

import akka.stream.scaladsl.Source
import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitUtils
import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitUtils.{handleBiscuitErrors, readOrWrite}
import org.biscuitsec.biscuit.datalog.RunLimits
import org.biscuitsec.biscuit.error.Error
import org.biscuitsec.biscuit.token.Biscuit
import org.biscuitsec.biscuit.token.builder.Utils.{fact, string}
import org.biscuitsec.biscuit.token.builder.parser.Parser
import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models.{ApiKey, EntityLocation, EntityLocationSupport, PrivateAppsUser}
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.next.models.NgRoute
import otoroshi.next.utils.JsonHelpers
import otoroshi.security.IdGenerator
import otoroshi.storage.{BasicStore, RedisLike, RedisLikeStore}
import otoroshi.utils.TypedMap
import otoroshi.utils.http.RequestImplicits.EnhancedRequestHeader
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtension, BiscuitExtensionDatastores, BiscuitExtensionState}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.RequestHeader

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

case class VerificationContext(route: NgRoute, request: RequestHeader, user: Option[PrivateAppsUser], apikey: Option[ApiKey], attrs: TypedMap) {
  def json: JsObject = Json.obj(
    "apikey" -> apikey.map(_.lightJson).getOrElse(JsNull).as[JsValue],
    "user" -> user.map(_.lightJson).getOrElse(JsNull).as[JsValue],
    "request" -> JsonHelpers.requestToJson(request, attrs),
    "route" -> route.json,
  )
}

case class VerifierConfig(
  checks: Seq[String] = Seq.empty,
  facts: Seq[String] = Seq.empty,
  resources: Seq[String] = Seq.empty,
  rules: Seq[String] = Seq.empty,
  policies: Seq[String] = Seq.empty,
  revokedIds: Seq[String] = Seq.empty,
  rbacPolicyRefs: Seq[String] = Seq.empty,
  remoteFactsRefs: Seq[String] = Seq.empty,
) {
  private val logger = Logger("otoroshi-biscuit-studio-verifier")
  def json: JsValue = VerifierConfig.format.writes(this)
  def verify(biscuitToken: Biscuit, ctxOpt: Option[VerificationContext])(implicit env: Env, ec: ExecutionContext): Future[Either[String, Unit]] = {

    val verifier = biscuitToken.authorizer()
    verifier.set_time()

    if (ctxOpt.nonEmpty) {
      val ctx = ctxOpt.get

      verifier.add_fact(s"""operation("${readOrWrite(ctx.request.method)}")""")

      verifier.add_fact(
        fact(
          "resource",
          Seq(
            string(ctx.request.method.toLowerCase),
            string(ctx.request.domain),
            string(ctx.request.path)
          ).asJava
        )
      )
      verifier.add_fact(fact("hostname", Seq(string(ctx.request.theHost)).asJava))
      verifier.add_fact(fact("resource", Seq(string(ctx.request.domain)).asJava))
      verifier.add_fact(fact("req_path", Seq(string(ctx.request.path)).asJava))
      verifier.add_fact(fact("req_domain", Seq(string(ctx.request.domain)).asJava))
      verifier.add_fact(fact("req_method", Seq(string(ctx.request.method.toLowerCase)).asJava))
      verifier.add_fact(fact("route_id", Seq(string(ctx.route.id)).asJava))
      verifier.add_fact(fact("ip_address", Seq(string(ctx.request.theIpAddress)).asJava))
      verifier.add_fact(fact("user_agent", Seq(string(ctx.request.theUserAgent)).asJava))
      verifier.add_fact(fact("req_protocol", Seq(string(ctx.request.theProtocol)).asJava))

      if (ctx.user.isDefined) {
        verifier.add_fact(fact("user_name", Seq(string(ctx.user.get.name)).asJava))
        verifier.add_fact(fact("user_email", Seq(string(ctx.user.get.email)).asJava))
        verifier.add_fact(fact("auth_method", Seq(string("user")).asJava))

        ctx.user.get.tags.foreach { tag => {
          verifier.add_fact(fact("user_tag", Seq(string(tag)).asJava))
        }
        }

        ctx.user.get.metadata.foreach {
          case (key, value) => verifier.add_fact(fact("user_metadata", Seq(string(key), string(value)).asJava))
        }
      }

      if (ctx.apikey.isDefined) {
        verifier.add_fact(fact("auth_method", Seq(string("apikey")).asJava))
        verifier.add_fact(fact("apikey_client_id", Seq(string(ctx.apikey.get.clientId)).asJava))
        verifier.add_fact(fact("apikey_client_name", Seq(string(ctx.apikey.get.clientName)).asJava))

        ctx.apikey.get.tags.foreach { tag => {
          verifier.add_fact(fact("apikey_tag", Seq(string(tag)).asJava))
        }
        }

        ctx.apikey.get.metadata.foreach {
          case (key, value) => verifier.add_fact(fact("apikey_metadata", Seq(string(key), string(value)).asJava))
        }
      }

      ctx.request.headers.headers.map { case (headerName, headerValue) =>
        verifier.add_fact(fact(
          "req_headers",
          Seq(string(headerName.toLowerCase), string(headerValue)).asJava
        ))
      }
    }

    // Add resources from the configuration
    resources
      .map(_.trim.stripSuffix(";"))
      .foreach(r => verifier.add_fact(s"""resource("${r}")"""))

    // Checks
    checks
      .map(_.trim.stripSuffix(";"))
      .map(Parser.check)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_check(r))

    // Facts
    facts
      .map(_.trim.stripSuffix(";"))
      .map(Parser.fact)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_fact(r))

    // Rules
    rules
      .map(_.trim.stripSuffix(";"))
      .map(Parser.rule)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_rule(r))

    // Policies : allow or deny
    policies
      .map(_.trim.stripSuffix(";"))
      .map(Parser.policy)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_policy(r))

    if (rbacPolicyRefs.nonEmpty) {
      rbacPolicyRefs.foreach { rbacPolicyRef =>
        env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitRbacPolicy(rbacPolicyRef)) match {
          case None => ()
          case Some(rbacPolicyConf) => {
            val rbacConf = rbacPolicyConf.roles
              .map(r => s"""role("${r._1}", ${r._2})""")
              .map(_.stripSuffix(";"))
              .toSeq
            rbacConf.foreach(f => verifier.add_fact(f))
          }
        }
      }
    }

    val remoteFactsF: Future[RemoteFactsData] = if (remoteFactsRefs.isEmpty) {
      RemoteFactsData().vfuture
    } else {
      Source(remoteFactsRefs.toList)
        .mapAsync(1) { remoteFactsRef =>
          env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitRemoteFactsLoader(remoteFactsRef)) match {
            case None => RemoteFactsData().vfuture // TODO: log error though
            case Some(remoteFactsEntity) => {
              if (remoteFactsEntity.config.apiUrl.nonEmpty && remoteFactsEntity.config.headers.nonEmpty) {
                val jsonCtx = ctxOpt.map(_.json.asObject).getOrElse(Json.obj()) ++ Json.obj("phase" -> "access", "plugin" -> "biscuit_verifier")
                remoteFactsEntity.config.getRemoteFacts(jsonCtx).map {
                  case Left(err) => {
                    RemoteFactsData()
                  }
                  case Right(factsData) => factsData
                }
              } else {
                RemoteFactsData().vfuture
              }
            }
          }
        }
        .runFold(RemoteFactsData())(_.merge(_))(env.otoroshiMaterializer)
    }

    remoteFactsF.flatMap { remoteFacts =>

      (remoteFacts.facts ++ remoteFacts.roles ++ remoteFacts.acl)
        .map(_.trim.stripSuffix(";"))
        .map(Parser.fact)
        .filter(_.isRight)
        .map(_.get()._2)
        .foreach(r => verifier.add_fact(r))

      remoteFacts.checks
        .map(_.trim.stripSuffix(";"))
        .map(Parser.check)
        .filter(_.isRight)
        .map(_.get()._2)
        .foreach(r => verifier.add_check(r))

      val listOfTokenRevocationIds = biscuitToken.revocation_identifiers().asScala.map(_.toHex).toList

      env.adminExtensions.extension[BiscuitExtension].get.datastores.biscuitRevocationDataStore.existsAny(listOfTokenRevocationIds).flatMap{
        existAnyRevokedToken =>
          if (existAnyRevokedToken) {
            Left("Token is revoked").vfuture
          } else {
            // Check for token revocation from verifier configuration and remote facts
            val revocationList = revokedIds ++ remoteFacts.revoked
            val tokenRevocationList = biscuitToken.revocation_identifiers().asScala.map(_.toHex).toList
            if (revocationList.exists(rvk => tokenRevocationList.contains(rvk))) {
              Left("Token is revoked").vfuture
            } else {
              val maxFacts = env.adminExtensions.extension[BiscuitExtension].get.configuration.getOptional[Int]("verifier_run_limit.max_facts").getOrElse(1000)
              val maxIterations = env.adminExtensions.extension[BiscuitExtension].get.configuration.getOptional[Int]("verifier_run_limit.max_iterations").getOrElse(100)
              val maxTime = java.time.Duration.ofMillis(env.adminExtensions.extension[BiscuitExtension].get.configuration.getOptional[Long]("verifier_run_limit.max_time").getOrElse(1000))
              // Perform authorization
              if (verifier.policies().isEmpty) {
                Try(verifier.allow().authorize(new RunLimits(maxFacts, maxIterations, maxTime))).toEither match {
                  case Left(err: org.biscuitsec.biscuit.error.Error) =>
                    Left(handleBiscuitErrors(err)).vfuture
                  case Left(err) =>
                    Left(handleBiscuitErrors(new org.biscuitsec.biscuit.error.Error.InternalError())).vfuture
                  case Right(_) =>
                    Right(()).vfuture
                }
              } else {
                Try(verifier.authorize(new RunLimits(maxFacts, maxIterations, maxTime))).toEither match {
                  case Left(err: org.biscuitsec.biscuit.error.Error) =>
                    Left(handleBiscuitErrors(err)).vfuture
                  case Left(err) =>
                    Left(handleBiscuitErrors(new org.biscuitsec.biscuit.error.Error.InternalError())).vfuture
                  case Right(_) =>
                    Right(()).vfuture
                }
              }
            }
          }
      }
    }
  }
}

object VerifierConfig {
  val format = new Format[VerifierConfig] {
    override def writes(o: VerifierConfig): JsValue = {
      Json.obj(
        "checks" -> o.checks,
        "facts" -> o.facts,
        "resources" -> o.resources,
        "rules" -> o.rules,
        "policies" -> o.policies,
        "rbac_refs" -> o.rbacPolicyRefs,
        "revoked_ids" -> o.revokedIds,
        "remote_facts_refs" -> o.remoteFactsRefs,
      )
    }

    override def reads(json: JsValue): JsResult[VerifierConfig] =
      Try {
        VerifierConfig(
          checks = (json \ "checks").asOpt[Seq[String]].getOrElse(Seq.empty),
          facts = (json \ "facts").asOpt[Seq[String]].getOrElse(Seq.empty),
          resources = (json \ "resources").asOpt[Seq[String]].getOrElse(Seq.empty),
          rules = (json \ "rules").asOpt[Seq[String]].getOrElse(Seq.empty),
          policies = (json \ "policies").asOpt[Seq[String]].getOrElse(Seq.empty),
          rbacPolicyRefs = (json \ "rbac_refs").asOpt[Seq[String]].getOrElse(Seq.empty),
          revokedIds = (json \ "revoked_ids").asOpt[Seq[String]].getOrElse(Seq.empty),
          remoteFactsRefs = (json \ "remote_facts_refs").asOpt[Seq[String]].getOrElse(Seq.empty)
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }
}

case class BiscuitExtractorConfig(
  extractorType: String = "header",
  extractorName: String = "Authorization"
) {

  def json: JsValue = BiscuitExtractorConfig.format.writes(this)

  def extractToken(req: RequestHeader, user: Option[PrivateAppsUser], attrParams: TypedMap): Option[String] = {
    (extractorType match {
      case "header" => req.headers.get(extractorName)
      case "query" => req.getQueryString(extractorName)
      case "cookie" => req.cookies.get(extractorName).map(_.value)
      case "user_tokens" => user.map(_.token).getOrElse(Json.obj()).at(extractorName).asOpt[String]
      case "attrs" => attrParams.get(BiscuitUtils.BiscuitTokenKey).map(_._1)
      case _ => None
    }).map { token =>
      BiscuitExtractorConfig.replaceHeader(token)
    }
  }
}

object BiscuitExtractorConfig {
  val format = new Format[BiscuitExtractorConfig] {
    override def reads(json: JsValue): JsResult[BiscuitExtractorConfig] = Try {
      BiscuitExtractorConfig(
        extractorType = json.select("extractor_type").asOpt[String].getOrElse("header"),
        extractorName = json.select("extractor_name").asOpt[String].getOrElse("Authorization")
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }

    override def writes(o: BiscuitExtractorConfig): JsValue = Json.obj(
      "extractor_type" -> o.extractorType,
      "extractor_name" -> o.extractorName,
    )
  }

  def replaceHeader(token: String): String = {
    token
      .replace("Bearer ", "")
      .replace("Bearer: ", "")
      .replace("Bearer:", "")
      .replace("Biscuit ", "")
      .replace("Biscuit: ", "")
      .replace("Biscuit-Token ", "")
      .replace("Biscuit-Token", "")
      .replace("BiscuitToken ", "")
      .replace("BiscuitToken", "")
      .replace("biscuit: ", "")
      .replace("biscuit:", "")
      .replace("sealed-biscuit: ", "")
      .replace("sealed-biscuit:", "")
      .trim
  }
}

case class BiscuitVerifier(
  id: String,
  name: String = "",
  description: String = "",
  strict: Boolean = true,
  enabled: Boolean = true,
  tags: Seq[String] = Seq.empty,
  metadata: Map[String, String] = Map.empty,
  location: EntityLocation = EntityLocation.default,
  keypairRef: String = "",
  config: VerifierConfig,
  extractor: BiscuitExtractorConfig,
) extends EntityLocationSupport {
  def json: JsValue = BiscuitVerifier.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags

  def verifyBase64Token(token: String, ctxOpt: Option[VerificationContext], attrs: TypedMap)(implicit env: Env, ec: ExecutionContext): Future[Either[String, Unit]] = {
    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keypairRef)) match {
      case None => Left("keypair_ref not found").vfuture
      case Some(keypair) => {
        Try(Biscuit.from_b64url(token, keypair.getPubKey)).toEither match {
          case Left(err) => Left(s"Unable to deserialize Biscuit token : ${err}").vfuture
          case Right(biscuitToken) => config.verify(biscuitToken, ctxOpt)
        }
      }
    }
  }

  def verify(req: RequestHeader, ctxOpt: Option[VerificationContext], attrs: TypedMap)(implicit env: Env, ec: ExecutionContext): Future[Either[String, Unit]] = {
    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keypairRef)) match {
      case None => Left("keypair_ref not found").vfuture
      case Some(keypair) => {
        extractor.extractToken(req, ctxOpt.flatMap(_.user), attrs) match {
          case Some(token) => {
            Try(Biscuit.from_b64url(token, keypair.getPubKey)).toEither match {
              case Left(err) => Left(s"Unable to deserialize Biscuit token : ${err}").vfuture
              case Right(biscuitToken) => config.verify(biscuitToken, ctxOpt)
            }
          }
          case _ => Left("no token").vfuture
        }
      }
    }
  }
}

object BiscuitVerifier {
  val format = new Format[BiscuitVerifier] {
    override def writes(o: BiscuitVerifier): JsValue = {
      Json.obj(
        "enabled" -> o.enabled,
        "id" -> o.id,
        "keypair_ref" -> o.keypairRef,
        "name" -> o.name,
        "description" -> o.description,
        "metadata" -> o.metadata,
        "strict" -> o.strict,
        "tags" -> JsArray(o.tags.map(JsString.apply)),
        "config" -> o.config.json,
        "extractor" -> o.extractor.json,
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitVerifier] =
      Try {
        BiscuitVerifier(
          location = EntityLocation.readFromKey(json),
          keypairRef = (json \ "keypair_ref").asOpt[String].getOrElse(""),
          id = (json \ "id").as[String],
          name = (json \ "name").as[String],
          description = (json \ "description").asOpt[String].getOrElse("--"),
          enabled = (json \ "enabled").asOpt[Boolean].getOrElse(true),
          strict = (json \ "strict").asOpt[Boolean].getOrElse(true),
          metadata = (json \ "metadata").asOpt[Map[String, String]].getOrElse(Map.empty),
          tags = (json \ "tags").asOpt[Seq[String]].getOrElse(Seq.empty[String]),
          config = json.select("config").asOpt(VerifierConfig.format).getOrElse(VerifierConfig()),
          extractor = json.select("extractor").asOpt(BiscuitExtractorConfig.format).getOrElse(BiscuitExtractorConfig()),
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }

  def resource(env: Env, datastores: BiscuitExtensionDatastores, states: BiscuitExtensionState): Resource = {
    Resource(
      "BiscuitVerifier",
      "biscuit-verifiers",
      "biscuit-verifier",
      "biscuit.extensions.cloud-apim.com",
      ResourceVersion("v1", true, false, true),
      GenericResourceAccessApiWithState[BiscuitVerifier](
        format = BiscuitVerifier.format,
        clazz = classOf[BiscuitVerifier],
        keyf = id => datastores.biscuitVerifierDataStore.key(id),
        extractIdf = c => datastores.biscuitVerifierDataStore.extractId(c),
        extractIdJsonf = json => json.select("id").asString,
        idFieldNamef = () => "id",
        tmpl = (v, p, ctx) => {
          BiscuitVerifier(
            id = IdGenerator.namedId("biscuit-verifier", env),
            name = "New biscuit verifier",
            description = "New biscuit verifier",
            location = EntityLocation.default,
            config = VerifierConfig(),
            extractor = BiscuitExtractorConfig(),
          ).json
        },
        canRead = true,
        canCreate = true,
        canUpdate = true,
        canDelete = true,
        canBulk = true,
        stateAll = () => states.allBiscuitVerifiers(),
        stateOne = id => states.biscuitVerifier(id),
        stateUpdate = values => states.updateBiscuitVerifiers(values)
      )
    )
  }
}

trait BiscuitVerifierDataStore extends BasicStore[BiscuitVerifier]

class KvBiscuitVerifierDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitVerifierDataStore
    with RedisLikeStore[BiscuitVerifier] {
  override def fmt: Format[BiscuitVerifier] = BiscuitVerifier.format

  override def redisLike(implicit env: Env): RedisLike = redisCli

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:verifiers:$id"

  override def extractId(value: BiscuitVerifier): String = value.id
}