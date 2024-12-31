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
                              token: String,
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
          token = (json \ "token").asOpt[String].getOrElse("--"),
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
        BiscuitTokenForge.format,
        classOf[BiscuitTokenForge],
        datastores.biscuitTokenForgeDataStore.key,
        datastores.biscuitTokenForgeDataStore.extractId,
        json => json.select("id").asString,
        () => "id",
        (v, p) => datastores.biscuitTokenForgeDataStore.template(env).json,
        stateAll = () => states.allBiscuitTokenForge(),
        stateOne = id => states.biscuitTokenForge(id),
        stateUpdate = values => states.updateBiscuitTokenForge(values))
    )
  }
}

trait BiscuitTokenForgeDataStore extends BasicStore[BiscuitTokenForge] {
  def template(env: Env): BiscuitTokenForge = {
    val defaultBiscuitTokenForge = BiscuitTokenForge(
      id = IdGenerator.namedId("biscuit_token_", env),
      name = "New biscuit token",
      description = "New biscuit token",
      token = "",
      keypairRef = "",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default,
      config = None
    )
    env.datastores.globalConfigDataStore
      .latest()(env.otoroshiExecutionContext, env)
      .templates
      .apikey
      .map { template =>
        BiscuitTokenForge.format.reads(defaultBiscuitTokenForge.json.asObject.deepMerge(template)).get
      }
      .getOrElse {
        defaultBiscuitTokenForge
      }
  }
}

class KvBiscuitTokenForgeDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitTokenForgeDataStore
    with RedisLikeStore[BiscuitTokenForge] {
  override def fmt: Format[BiscuitTokenForge] = BiscuitTokenForge.format

  override def redisLike(implicit env: Env): RedisLike = redisCli

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:tokens:$id"

  override def extractId(value: BiscuitTokenForge): String = value.id
}