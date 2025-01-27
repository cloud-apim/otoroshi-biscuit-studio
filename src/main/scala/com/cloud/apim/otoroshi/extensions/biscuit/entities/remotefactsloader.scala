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


case class BiscuitRemoteFactsConfig(
                                     apiUrl: String,
                                     headers: Map[String, String],
                                   ) {
  def json: JsValue = BiscuitRemoteFactsConfig.format.writes(this)
}

object BiscuitRemoteFactsConfig {
  val format = new Format[BiscuitRemoteFactsConfig] {
    override def writes(o: BiscuitRemoteFactsConfig): JsValue = {
      Json.obj(
        "apiUrl" -> o.apiUrl,
        "headers" -> o.headers,
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitRemoteFactsConfig] =
      Try {
        BiscuitRemoteFactsConfig(
          apiUrl = (json \ "apiUrl").asOpt[String].getOrElse(""),
          headers = (json \ "headers").asOpt[Map[String, String]].getOrElse(Map.empty),
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }
}

case class RemoteFactsLoader(
                              id: String,
                              name: String,
                              description: String,
                              enabled: Boolean = true,
                              tags: Seq[String] = Seq.empty,
                              metadata: Map[String, String] = Map.empty,
                              location: EntityLocation,
                              config: Option[BiscuitRemoteFactsConfig]
                            ) extends EntityLocationSupport {
  def json: JsValue = RemoteFactsLoader.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags
}


object RemoteFactsLoader {
  val format = new Format[RemoteFactsLoader] {
    override def writes(o: RemoteFactsLoader): JsValue = {
      Json.obj(
        "enabled" -> o.enabled,
        "id" -> o.id,
        "name" -> o.name,
        "description" -> o.description,
        "metadata" -> o.metadata,
        "tags" -> JsArray(o.tags.map(JsString.apply)),
        "config" -> o.config.map(_.json).getOrElse(JsNull).asValue
      )
    }

    override def reads(json: JsValue): JsResult[RemoteFactsLoader] =
      Try {
        RemoteFactsLoader(
          location = EntityLocation.readFromKey(json),
          id = (json \ "id").as[String],
          name = (json \ "name").as[String],
          description = (json \ "description").asOpt[String].getOrElse("--"),
          enabled = (json \ "enabled").asOpt[Boolean].getOrElse(true),
          metadata = (json \ "metadata").asOpt[Map[String, String]].getOrElse(Map.empty),
          tags = (json \ "tags").asOpt[Seq[String]].getOrElse(Seq.empty[String]),
          config = BiscuitRemoteFactsConfig.format.reads(json.select("config").getOrElse(JsNull)).asOpt
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }

  def resource(env: Env, datastores: BiscuitExtensionDatastores, states: BiscuitExtensionState): Resource = {
    Resource(
      "BiscuitRemoteFactsLoader",
      "biscuit-remote-facts",
      "biscuit-remote-facts",
      "biscuit.extensions.cloud-apim.com",
      ResourceVersion("v1", true, false, true),
      GenericResourceAccessApiWithState[RemoteFactsLoader](
        format = RemoteFactsLoader.format,
        clazz = classOf[RemoteFactsLoader],
        keyf = id => datastores.biscuitRemoteFactsLoaderDataStore.key(id),
        extractIdf = c => datastores.biscuitRemoteFactsLoaderDataStore.extractId(c),
        extractIdJsonf = json => json.select("id").asString,
        idFieldNamef = () => "id",
        tmpl = (v, p) => {
          RemoteFactsLoader(
            id = IdGenerator.namedId("biscuit-remote-facts", env),
            name = "New biscuit remote facts loader",
            description = "New biscuit remote facts loader",
            metadata = Map.empty,
            tags = Seq.empty,
            location = EntityLocation.default,
            config = BiscuitRemoteFactsConfig(
              apiUrl = "https://my-api.domain.com/v1/facts",
              headers = Map(
                "Authorization" -> "Bearer xxxx",
                "Content-Type" -> "application/json"
              )
            ).some
          ).json
        },
        canRead = true,
        canCreate = true,
        canUpdate = true,
        canDelete = true,
        canBulk = true,
        stateAll = () => states.allBiscuitRemoteFactsLoader(),
        stateOne = id => states.biscuitRemoteFactsLoader(id),
        stateUpdate = values => states.updatebiscuitRemoteFactsLoader(values)
      )
    )
  }
}

trait BiscuitRemoteFactsLoaderDataStore extends BasicStore[RemoteFactsLoader]

class KvBiscuitRemoteFactsLoaderDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitRemoteFactsLoaderDataStore
    with RedisLikeStore[RemoteFactsLoader] {
  override def fmt: Format[RemoteFactsLoader] = RemoteFactsLoader.format

  override def redisLike(implicit env: Env): RedisLike = redisCli

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:remote-facts:$id"

  override def extractId(value: RemoteFactsLoader): String = value.id
}