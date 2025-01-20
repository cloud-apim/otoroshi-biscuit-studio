package com.cloud.apim.otoroshi.extensions.biscuit.entities

import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitForgeConfig
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionDatastores, BiscuitExtensionState}

import scala.util.{Failure, Success, Try}
import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models._
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.storage._
import otoroshi.utils.syntax.implicits._
import play.api.libs.json._
import otoroshi.security.IdGenerator

case class BiscuitTokenForge(
                              id: String,
                              name: String,
                              description: String,
                              token: Option[String],
                              keypairRef: String,
                              config: Option[BiscuitForgeConfig],
                              tags: Seq[String],
                              metadata: Map[String, String],
                              location: EntityLocation
                            ) extends EntityLocationSupport {
  def json: JsValue = BiscuitTokenForge.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags
}

object BiscuitTokenForge {
  val format = new Format[BiscuitTokenForge] {
    override def writes(o: BiscuitTokenForge): JsValue = {
      Json.obj(
        "id" -> o.id,
        "name" -> o.name,
        "description" -> o.description,
        "metadata" -> o.metadata,
        "keypair_ref" -> o.keypairRef,
        "config" -> o.config.map(_.json).getOrElse(JsNull).asValue,
        "token" -> o.token,
        "tags" -> JsArray(o.tags.map(JsString.apply))
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitTokenForge] =
      Try {
        BiscuitTokenForge(
          location = EntityLocation.readFromKey(json),
          id = (json \ "id").as[String],
          name = (json \ "name").as[String],
          description = (json \ "description").asOpt[String].getOrElse("--"),
          keypairRef = (json \ "keypair_ref").asOpt[String].getOrElse("--"),
          token = (json \ "token").asOpt[String],
          metadata = (json \ "metadata").asOpt[Map[String, String]].getOrElse(Map.empty),
          tags = (json \ "tags").asOpt[Seq[String]].getOrElse(Seq.empty[String]),
          config = BiscuitForgeConfig.format.reads(json.select("config").getOrElse(JsNull)).asOpt
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }

  def resource(env: Env, datastores: BiscuitExtensionDatastores, states: BiscuitExtensionState): Resource = {
    Resource(
      "BiscuitTokenForge",
      "tokens-forge",
      "tokens-forge",
      "biscuit.extensions.cloud-apim.com",
      ResourceVersion("v1", true, false, true),
      GenericResourceAccessApiWithState[BiscuitTokenForge](
        format = BiscuitTokenForge.format,
        clazz = classOf[BiscuitTokenForge],
        keyf = id => datastores.biscuitTokenForgeDataStore.key(id),
        extractIdf = c => datastores.biscuitTokenForgeDataStore.extractId(c),
        extractIdJsonf = json => json.select("id").asString,
        idFieldNamef = () => "id",
        tmpl = (v, p) => {
          BiscuitTokenForge(
            id = IdGenerator.namedId("biscuit-token", env),
            name = "New biscuit token",
            description = "New biscuit token",
            token = None,
            keypairRef = "",
            metadata = Map.empty,
            tags = Seq.empty,
            location = EntityLocation.default,
            config = BiscuitForgeConfig(
              checks = Seq.empty,
              facts = Seq.empty,
              resources = Seq.empty,
              rules = Seq.empty
            ).some
          ).json
        },
        canRead = true,
        canCreate = true,
        canUpdate = true,
        canDelete = true,
        canBulk = true,
        stateAll = () => states.allBiscuitTokenForge(),
        stateOne = id => states.biscuitTokenForge(id),
        stateUpdate = values => states.updateBiscuitTokenForge(values)
      )
    )
  }
}

trait BiscuitTokenForgeDataStore extends BasicStore[BiscuitTokenForge]

class KvBiscuitTokenForgeDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitTokenForgeDataStore
    with RedisLikeStore[BiscuitTokenForge] {
  override def fmt: Format[BiscuitTokenForge] = BiscuitTokenForge.format

  override def redisLike(implicit env: Env): RedisLike = redisCli

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:tokens:$id"

  override def extractId(value: BiscuitTokenForge): String = value.id
}