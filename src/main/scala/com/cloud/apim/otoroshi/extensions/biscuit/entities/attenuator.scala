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

case class AttenuatorConfig(
                             checks: Seq[String]
                           ) {
  def json: JsValue = AttenuatorConfig.format.writes(this)
}

object AttenuatorConfig {
  val format = new Format[AttenuatorConfig] {
    override def writes(o: AttenuatorConfig): JsValue = {
      Json.obj(
        "checks" -> o.checks
      )
    }

    override def reads(json: JsValue): JsResult[AttenuatorConfig] =
      Try {
        AttenuatorConfig(
          checks = (json \ "checks").asOpt[Seq[String]].getOrElse(Seq.empty)
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }
}

case class BiscuitAttenuator(
                              id: String,
                              name: String,
                              description: String,
                              enabled: Boolean = true,
                              tags: Seq[String] = Seq.empty,
                              metadata: Map[String, String] = Map.empty,
                              location: EntityLocation,
                              keypairRef: String,
                              config: Option[AttenuatorConfig]
                            ) extends EntityLocationSupport {
  def json: JsValue = BiscuitAttenuator.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags
}


object BiscuitAttenuator {
  val format = new Format[BiscuitAttenuator] {
    override def writes(o: BiscuitAttenuator): JsValue = {
      Json.obj(
        "enabled" -> o.enabled,
        "id" -> o.id,
        "keypair_ref" -> o.keypairRef,
        "name" -> o.name,
        "description" -> o.description,
        "metadata" -> o.metadata,
        "tags" -> JsArray(o.tags.map(JsString.apply)),
        "config" -> o.config.map(_.json).getOrElse(JsNull).asValue
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitAttenuator] =
      Try {
        BiscuitAttenuator(
          location = EntityLocation.readFromKey(json),
          id = (json \ "id").as[String],
          name = (json \ "name").as[String],
          keypairRef = (json \ "keypair_ref").asOpt[String].getOrElse(""),
          description = (json \ "description").asOpt[String].getOrElse("--"),
          enabled = (json \ "enabled").asOpt[Boolean].getOrElse(true),
          metadata = (json \ "metadata").asOpt[Map[String, String]].getOrElse(Map.empty),
          tags = (json \ "tags").asOpt[Seq[String]].getOrElse(Seq.empty[String]),
          config = AttenuatorConfig.format.reads(json.select("config").getOrElse(JsNull)).asOpt
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }

  def resource(env: Env, datastores: BiscuitExtensionDatastores, states: BiscuitExtensionState): Resource = {
    Resource(
      "BiscuitAttenuator",
      "biscuit-attenuators",
      "biscuit-attenuator",
      "biscuit.extensions.cloud-apim.com",
      ResourceVersion("v1", true, false, true),
      GenericResourceAccessApiWithState[BiscuitAttenuator](
        format = BiscuitAttenuator.format,
        clazz = classOf[BiscuitAttenuator],
        keyf = id => datastores.biscuitAttenuatorDataStore.key(id),
        extractIdf = c => datastores.biscuitAttenuatorDataStore.extractId(c),
        extractIdJsonf = json => json.select("id").asString,
        idFieldNamef = () => "id",
        tmpl = (v, p) => {
          BiscuitAttenuator(
            id = IdGenerator.namedId("biscuit-attenuator", env),
            name = "New biscuit Attenuator",
            description = "New biscuit Attenuator",
            metadata = Map.empty,
            tags = Seq.empty,
            location = EntityLocation.default,
            keypairRef = "",
            config = AttenuatorConfig(
              checks = Seq.empty
            ).some
          ).json
        },
        canRead = true,
        canCreate = true,
        canUpdate = true,
        canDelete = true,
        canBulk = true,
        stateAll = () => states.allBiscuitAttenuators(),
        stateOne = id => states.biscuitAttenuator(id),
        stateUpdate = values => states.updateBiscuitAttenuators(values)
      )
    )
  }
}

trait BiscuitAttenuatorDataStore extends BasicStore[BiscuitAttenuator]

class KvBiscuitAttenuatorDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitAttenuatorDataStore
    with RedisLikeStore[BiscuitAttenuator] {
  override def fmt: Format[BiscuitAttenuator] = BiscuitAttenuator.format

  override def redisLike(implicit env: Env): RedisLike = redisCli

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:attenuators:$id"

  override def extractId(value: BiscuitAttenuator): String = value.id
}