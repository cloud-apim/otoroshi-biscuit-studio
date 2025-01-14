package com.cloud.apim.otoroshi.extensions.biscuit.entities

import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models.{EntityLocation, EntityLocationSupport}
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.security.IdGenerator
import otoroshi.storage.{BasicStore, RedisLike, RedisLikeStore}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionDatastores, BiscuitExtensionState}
import play.api.libs.json._
import otoroshi.utils.syntax.implicits._

import scala.util.{Failure, Success, Try}

case class BiscuitRbacPolicy(
                              id: String,
                              name: String,
                              description: String,
                              strict: Boolean = true,
                              enabled: Boolean = true,
                              tags: Seq[String] = Seq.empty,
                              metadata: Map[String, String] = Map.empty,
                              location: EntityLocation,
                              roles: Map[String, JsArray] = Map.empty,
                            ) extends EntityLocationSupport {
  def json: JsValue = BiscuitRbacPolicy.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags
}


object BiscuitRbacPolicy {
  val format = new Format[BiscuitRbacPolicy] {
    override def writes(o: BiscuitRbacPolicy): JsValue = {
      Json.obj(
        "enabled" -> o.enabled,
        "id" -> o.id,
        "name" -> o.name,
        "description" -> o.description,
        "metadata" -> o.metadata,
        "strict" -> o.strict,
        "tags" -> JsArray(o.tags.map(JsString.apply)),
        "roles" -> o.roles
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitRbacPolicy] =
      Try {
        BiscuitRbacPolicy(
          location = EntityLocation.readFromKey(json),
          id = (json \ "id").as[String],
          name = (json \ "name").as[String],
          description = (json \ "description").asOpt[String].getOrElse("--"),
          enabled = (json \ "enabled").asOpt[Boolean].getOrElse(true),
          strict = (json \ "strict").asOpt[Boolean].getOrElse(true),
          metadata = (json \ "metadata").asOpt[Map[String, String]].getOrElse(Map.empty),
          tags = (json \ "tags").asOpt[Seq[String]].getOrElse(Seq.empty[String]),
          roles = (json \ "roles").asOpt[Map[String, JsArray]].getOrElse(Map.empty),
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }

  def resource(env: Env, datastores: BiscuitExtensionDatastores, states: BiscuitExtensionState): Resource = {
    Resource(
      "BiscuitRBAC",
      "biscuit-rbac",
      "biscuit-rbac",
      "biscuit.extensions.cloud-apim.com",
      ResourceVersion("v1", true, false, true),
      GenericResourceAccessApiWithState[BiscuitRbacPolicy](
        BiscuitRbacPolicy.format,
        classOf[BiscuitRbacPolicy],
        datastores.biscuitRbacPolicyDataStore.key,
        datastores.biscuitRbacPolicyDataStore.extractId,
        json => json.select("id").asString,
        () => "id",
        (v, p) => datastores.biscuitRbacPolicyDataStore.template(env).json,
        stateAll = () => states.allbiscuitRbacPolicies(),
        stateOne = id => states.biscuitRbacPolicy(id),
        stateUpdate = values => states.updateBiscuitRbacPolicy(values))
    )
  }
}

trait BiscuitRbacPolicyDataStore extends BasicStore[BiscuitRbacPolicy] {
  def template(env: Env): BiscuitRbacPolicy = {
    val defaultBiscuitRbacPolicy = BiscuitRbacPolicy(
      id = IdGenerator.namedId("biscuit-rbac-policy", env),
      name = "New biscuit RBAC Policy",
      description = "New biscuit RBAC Policy",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      roles = Map.empty
    )
    env.datastores.globalConfigDataStore
      .latest()(env.otoroshiExecutionContext, env)
      .templates
      .apikey
      .map { template =>
        BiscuitRbacPolicy.format.reads(defaultBiscuitRbacPolicy.json.asObject.deepMerge(template)).get
      }
      .getOrElse {
        defaultBiscuitRbacPolicy
      }
  }
}

class KvBiscuitRbacPolicyDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitRbacPolicyDataStore
    with RedisLikeStore[BiscuitRbacPolicy] {
  override def fmt: Format[BiscuitRbacPolicy] = BiscuitRbacPolicy.format

  override def redisLike(implicit env: Env): RedisLike = redisCli

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:rbac-policy:$id"

  override def extractId(value: BiscuitRbacPolicy): String = value.id
}