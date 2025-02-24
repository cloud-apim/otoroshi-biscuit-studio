package com.cloud.apim.otoroshi.extensions.biscuit.entities

import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitUtils.{handleBiscuitErrors, readOrWrite}
import org.biscuitsec.biscuit.datalog.RunLimits
import org.biscuitsec.biscuit.error.Error
import org.biscuitsec.biscuit.token.Biscuit
import org.biscuitsec.biscuit.token.builder.Utils.{fact, string}
import org.biscuitsec.biscuit.token.builder.parser.Parser
import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models.{EntityLocation, EntityLocationSupport}
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.plugins.biscuit.VerificationContext
import otoroshi.security.IdGenerator
import otoroshi.storage.{BasicStore, RedisLike, RedisLikeStore}
import otoroshi.utils.http.RequestImplicits.EnhancedRequestHeader
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionDatastores, BiscuitExtensionState}
import play.api.libs.json._

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

case class VerifierConfig(
                           checks: Seq[String] = Seq.empty,
                           facts: Seq[String] = Seq.empty,
                           resources: Seq[String] = Seq.empty,
                           rules: Seq[String] = Seq.empty,
                           policies: Seq[String] = Seq.empty,
                           revokedIds: Seq[String] = Seq.empty,
                           rbacPolicyRef: Option[String] = None,
                           enableRemoteFacts: Boolean = false,
                           remoteFactsRef: Option[String] = None,
                         ) {
  def json: JsValue = VerifierConfig.format.writes(this)
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
        "rbac_ref" -> o.rbacPolicyRef,
        "enable_remote_facts" -> o.enableRemoteFacts,
        "remote_facts_ref" -> o.remoteFactsRef,
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
          rbacPolicyRef = (json \ "rbac_ref").asOpt[String],
          enableRemoteFacts = (json \ "enable_remote_facts").asOpt[Boolean].getOrElse(false),
          remoteFactsRef = (json \ "remote_facts_ref").asOpt[String]
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }
}

case class BiscuitVerifier(
                            id: String,
                            name: String,
                            description: String,
                            strict: Boolean = true,
                            enabled: Boolean = true,
                            tags: Seq[String] = Seq.empty,
                            metadata: Map[String, String] = Map.empty,
                            location: EntityLocation,
                            keypairRef: String = "",
                            config: VerifierConfig
                          ) extends EntityLocationSupport {
  def json: JsValue = BiscuitVerifier.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags

  def verify(biscuitToken: Biscuit, ctxOpt: Option[VerificationContext])(implicit env: Env): Either[String, Unit] = {

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
      verifier.add_fact(fact("route_id", Seq(string(ctx.descriptor.id)).asJava))
      verifier.add_fact(fact("ip_address", Seq(string(ctx.request.theIpAddress)).asJava))
      verifier.add_fact(fact("user_agent", Seq(string(ctx.request.theUserAgent)).asJava))
      verifier.add_fact(fact("req_protocol", Seq(string(ctx.request.theProtocol)).asJava))

      if (ctx.user.isDefined) {
        verifier.add_fact(fact("user_name", Seq(string(ctx.user.get.name)).asJava))
        verifier.add_fact(fact("user_email", Seq(string(ctx.user.get.email)).asJava))
        verifier.add_fact(fact("auth_method", Seq(string("user")).asJava))

        ctx.user.get.tags.foreach{tag => {
          verifier.add_fact(fact("user_tag", Seq(string(tag)).asJava))
        }}

        ctx.user.get.metadata.foreach{
          case (key, value) => verifier.add_fact(fact("user_metadata", Seq(string(key), string(value)).asJava))
        }
      }

      if (ctx.apikey.isDefined) {
        verifier.add_fact(fact("auth_method", Seq(string("apikey")).asJava))
        verifier.add_fact(fact("apikey_client_id", Seq(string(ctx.apikey.get.clientId)).asJava))
        verifier.add_fact(fact("apikey_client_name", Seq(string(ctx.apikey.get.clientName)).asJava))

        ctx.apikey.get.tags.foreach{tag => {
          verifier.add_fact(fact("apikey_tag", Seq(string(tag)).asJava))
        }}

        ctx.apikey.get.metadata.foreach{
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
    config.resources
      .map(_.trim.stripSuffix(";"))
      .foreach(r => verifier.add_fact(s"""resource("${r}")"""))

    // Checks
    config.checks
      .map(_.trim.stripSuffix(";"))
      .map(Parser.check)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_check(r))

    // Facts
    config.facts
      .map(_.trim.stripSuffix(";"))
      .map(Parser.fact)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_fact(r))

    // Rules
    config.rules
      .map(_.trim.stripSuffix(";"))
      .map(Parser.rule)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_rule(r))

    // Policies : allow or deny
    config.policies
      .map(_.trim.stripSuffix(";"))
      .map(Parser.policy)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_policy(r))

    // Check for token revocation
    val revokedIds = biscuitToken.revocation_identifiers().asScala.map(_.toHex).toList
    if (config.revokedIds.nonEmpty && config.revokedIds.exists(revokedIds.contains)) {
      return Left(handleBiscuitErrors(new Error.FormatError.DeserializationError("Revoked token")))
    }

    val maxFacts = 1000
    val maxIterations = 100
    val maxTime = java.time.Duration.ofMillis(100)

    // Perform authorization
    if (verifier.policies().isEmpty) {
      Try(verifier.allow().authorize(new RunLimits(maxFacts, maxIterations, maxTime))).toEither match {
        case Left(err: org.biscuitsec.biscuit.error.Error) =>
          Left(handleBiscuitErrors(err))
        case Left(err) =>
          Left(handleBiscuitErrors(new org.biscuitsec.biscuit.error.Error.InternalError()))
        case Right(_) =>
          Right(())
      }
    } else {
      Try(verifier.authorize(new RunLimits(maxFacts, maxIterations, maxTime))).toEither match {
        case Left(err: org.biscuitsec.biscuit.error.Error) =>
          Left(handleBiscuitErrors(err))
        case Left(err) =>
          Left(handleBiscuitErrors(new org.biscuitsec.biscuit.error.Error.InternalError()))
        case Right(_) =>
          Right(())
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
        "config" -> o.config.json
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
          config = json.select("config").asOpt(VerifierConfig.format).getOrElse(VerifierConfig())

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
        tmpl = (v, p) => {
          BiscuitVerifier(
            id = IdGenerator.namedId("biscuit-verifier", env),
            name = "New biscuit verifier",
            description = "New biscuit verifier",
            location = EntityLocation.default,
            config = VerifierConfig()
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