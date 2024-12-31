package com.cloud.apim.otoroshi.extensions.biscuit.entities

import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models.{EntityLocation, EntityLocationSupport}
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.security.IdGenerator
import otoroshi.storage.{BasicStore, RedisLike, RedisLikeStore}
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionDatastores, BiscuitExtensionState}
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

case class ExternalFactsConfig(
                                apiUrl: String
                              )

case class VerifierConfig(
                           checks: Seq[String],
                           facts: Seq[String],
                           resources: Seq[String],
                           rules: Seq[String],
                           revocation_ids: Seq[String]
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
        "revocation_ids" -> o.revocation_ids
      )
    }

    override def reads(json: JsValue): JsResult[VerifierConfig] =
      Try {
        VerifierConfig(
          checks = (json \ "checks").asOpt[Seq[String]].getOrElse(Seq.empty),
          facts = (json \ "facts").asOpt[Seq[String]].getOrElse(Seq.empty),
          resources = (json \ "resources").asOpt[Seq[String]].getOrElse(Seq.empty),
          rules = (json \ "rules").asOpt[Seq[String]].getOrElse(Seq.empty),
          revocation_ids = (json \ "revocation_ids").asOpt[Seq[String]].getOrElse(Seq.empty)
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
                            keypairRef: String,
                            config: Option[VerifierConfig]
                          ) extends EntityLocationSupport {
  def json: JsValue = BiscuitVerifier.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags
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
        "config" -> o.config.map(_.json).getOrElse(JsNull).asValue
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
          config = VerifierConfig.format.reads(json.select("config").getOrElse(JsNull)).asOpt
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
        BiscuitVerifier.format,
        classOf[BiscuitVerifier],
        datastores.biscuitVerifierDataStore.key,
        datastores.biscuitVerifierDataStore.extractId,
        json => json.select("id").asString,
        () => "id",
        (v, p) => datastores.biscuitVerifierDataStore.template(env).json,
        stateAll = () => states.allBiscuitVerifiers(),
        stateOne = id => states.biscuitVerifier(id),
        stateUpdate = values => states.updateBiscuitVerifiers(values))
    )
  }
}

trait BiscuitVerifierDataStore extends BasicStore[BiscuitVerifier] {
  def template(env: Env): BiscuitVerifier = {
    val defaultBiscuitVerifier = BiscuitVerifier(
      id = IdGenerator.namedId("biscuit_verifier", env),
      name = "New biscuit verifier",
      description = "New biscuit verifier",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      keypairRef = "",
      config = None
    )
    env.datastores.globalConfigDataStore
      .latest()(env.otoroshiExecutionContext, env)
      .templates
      .apikey
      .map { template =>
        BiscuitVerifier.format.reads(defaultBiscuitVerifier.json.asObject.deepMerge(template)).get
      }
      .getOrElse {
        defaultBiscuitVerifier
      }
  }
}

class KvBiscuitVerifierDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitVerifierDataStore
    with RedisLikeStore[BiscuitVerifier] {
  override def fmt: Format[BiscuitVerifier] = BiscuitVerifier.format

  override def redisLike(implicit env: Env): RedisLike = redisCli

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:verifiers:$id"

  override def extractId(value: BiscuitVerifier): String = value.id
}