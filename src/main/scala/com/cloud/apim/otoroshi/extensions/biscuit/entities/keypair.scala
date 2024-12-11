package com.cloud.apim.otoroshi.extensions.biscuit.entities

import org.biscuitsec.biscuit.crypto.KeyPair
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

case class BiscuitKeyPair (
                            id: String,
                            name: String,
                            description: String,
                            privKey: String,
                            pubKey: String,
                            tags: Seq[String],
                            metadata: Map[String, String],
                            location: EntityLocation
                          ) extends EntityLocationSupport {
  def json: JsValue                    = BiscuitKeyPair.format.writes(this)
  def internalId: String               = id
  def theDescription: String           = description
  def theMetadata: Map[String, String] = metadata
  def theName: String                  = name
  def theTags: Seq[String]             = tags
}

object BiscuitKeyPair{
  val format = new Format[BiscuitKeyPair] {
    override def writes(o: BiscuitKeyPair): JsValue = {
      Json.obj(
        "id"            -> o.id,
        "name"          -> o.name,
        "description"          -> o.description,
        "metadata"      -> o.metadata,
        "pubKey"        -> o.pubKey,
        "privKey"       -> o.privKey,
        "tags"          -> JsArray(o.tags.map(JsString.apply)),
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitKeyPair] =
      Try {
        BiscuitKeyPair(
          location = EntityLocation.readFromKey(json),
          id = (json \ "id").as[String],
          name = (json \ "name").as[String],
          description = (json \ "description").asOpt[String].getOrElse("--"),
          pubKey = (json \ "pubKey").asOpt[String].getOrElse("--"),
          privKey = (json \ "privKey").asOpt[String].getOrElse("--"),
          metadata = (json \ "metadata").asOpt[Map[String, String]].getOrElse(Map.empty),
          tags = (json \ "tags").asOpt[Seq[String]].getOrElse(Seq.empty[String]),
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }

  def resource(env: Env, datastores: BiscuitExtensionDatastores, states: BiscuitExtensionState): Resource = {
    Resource(
      "BiscuitKeyPair",
      "biscuit-keypairs",
      "biscuit-keypair",
      "biscuit.extensions.cloud-apim.com",
      ResourceVersion("v1", true, false, true),
      GenericResourceAccessApiWithState[BiscuitKeyPair](
        BiscuitKeyPair.format,
        classOf[BiscuitKeyPair],
        datastores.biscuitKeyPairDataStore.key,
        datastores.biscuitKeyPairDataStore.extractId,
        json => json.select("id").asString,
        () => "id",
        (v, p) => datastores.biscuitKeyPairDataStore.template(env).json,
        stateAll = () => states.allKeypairs(),
        stateOne = id => states.keypair(id),
        stateUpdate = values => states.updateKeyPairs(values))
      )
  }
}

trait BiscuitKeyPairDataStore extends BasicStore[BiscuitKeyPair]{
  def template(env: Env): BiscuitKeyPair = {
    val defaultBiscuitKeyPair = BiscuitKeyPair(
      id = IdGenerator.namedId("biscuit_keypair", env),
      name = "New biscuit keypair",
      description = "New biscuit keypair",
      pubKey = "",
      privKey = "",
      metadata = Map.empty,
      tags = Seq.empty,
      location = EntityLocation.default
    )
    env.datastores.globalConfigDataStore
      .latest()(env.otoroshiExecutionContext, env)
      .templates
      .apikey
      .map { template =>
        BiscuitKeyPair.format.reads(defaultBiscuitKeyPair.json.asObject.deepMerge(template)).get
      }
      .getOrElse {
        defaultBiscuitKeyPair
      }
  }
}

class KvBiscuitKeyPairDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitKeyPairDataStore
    with RedisLikeStore[BiscuitKeyPair] {
  override def fmt: Format[BiscuitKeyPair]                  = BiscuitKeyPair.format
  override def redisLike(implicit env: Env): RedisLike = redisCli
  override def key(id: String): String                 = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:keypairs:$id"
  override def extractId(value: BiscuitKeyPair): String    = value.id
}