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
                              roles: Map[String, Seq[String]] = Map.empty,
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
          roles = (json \ "roles").asOpt[Map[String, Seq[String]]].getOrElse(Map.empty),
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
        format = BiscuitRbacPolicy.format,
        clazz = classOf[BiscuitRbacPolicy],
        keyf = id => datastores.biscuitRbacPolicyDataStore.key(id),
        extractIdf = c => datastores.biscuitRbacPolicyDataStore.extractId(c),
        extractIdJsonf = json => json.select("id").asString,
        idFieldNamef = () => "id",
        tmpl = (v, p) => {
          BiscuitRbacPolicy(
            id = IdGenerator.namedId("biscuit-rbac-policy", env),
            name = "New biscuit RBAC Policy",
            description = "New biscuit RBAC Policy",
            metadata = Map.empty,
            tags = Seq.empty,
            location = EntityLocation.default,
            roles = Map(
              "admin" -> Seq("billing:read", "billing:write", "address:read", "address:write"),
              "accounting" -> Seq("billing:read", "billing:write", "address:read"),
              "pilot" -> Seq("spaceship:drive", "address:read"),
              "delivery" -> Seq("address:read", "package:load", "package:unload", "package:deliver")
            )
          ).json
        },
        canRead = true,
        canCreate = true,
        canUpdate = true,
        canDelete = true,
        canBulk = true,
        stateAll = () => states.allbiscuitRbacPolicies(),
        stateOne = id => states.biscuitRbacPolicy(id),
        stateUpdate = values => states.updateBiscuitRbacPolicy(values)
      )
    )
  }
}
trait BiscuitRbacPolicyDataStore extends BasicStore[BiscuitRbacPolicy]
class KvBiscuitRbacPolicyDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitRbacPolicyDataStore
    with RedisLikeStore[BiscuitRbacPolicy] {
  override def fmt: Format[BiscuitRbacPolicy] = BiscuitRbacPolicy.format

  override def redisLike(implicit env: Env): RedisLike = redisCli

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:rbac-policy:$id"

  override def extractId(value: BiscuitRbacPolicy): String = value.id
}