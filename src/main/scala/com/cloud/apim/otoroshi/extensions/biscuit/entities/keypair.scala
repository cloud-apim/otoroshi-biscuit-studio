package com.cloud.apim.otoroshi.extensions.biscuit.entities

import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitUtils
import org.biscuitsec.biscuit.crypto.{KeyPair, PublicKey}
import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models._
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.security.IdGenerator
import otoroshi.storage._
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionDatastores, BiscuitExtensionState}
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

case class BiscuitKeyPair(
  id: String,
  name: String = "",
  description: String = "",
  isPublic: Boolean = false,
  privKey: String,
  pubKey: String,
  algo: String = "ED25519",
  tags: Seq[String] = Seq.empty,
  metadata: Map[String, String] = Map.empty,
  location: EntityLocation
) extends EntityLocationSupport {
  def json: JsValue = BiscuitKeyPair.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags

  def keyPair: KeyPair = new KeyPair(privKey)
  def getPubKey: PublicKey = new PublicKey(getCurrentAlgo, pubKey)
  def getCurrentAlgo: biscuit.format.schema.Schema.PublicKey.Algorithm = BiscuitUtils.getAlgo(algo)
}

object BiscuitKeyPair {
  val format = new Format[BiscuitKeyPair] {
    override def writes(o: BiscuitKeyPair): JsValue = {
      Json.obj(
        "id" -> o.id,
        "name" -> o.name,
        "description" -> o.description,
        "metadata" -> o.metadata,
        "is_public" -> o.isPublic,
        "algo" -> o.algo,
        "pubKey" -> o.pubKey,
        "privKey" -> o.privKey,
        "tags" -> JsArray(o.tags.map(JsString.apply)),
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitKeyPair] =
      Try {
        BiscuitKeyPair(
          location = EntityLocation.readFromKey(json),
          id = (json \ "id").as[String],
          name = (json \ "name").as[String],
          algo = (json \ "algo").asOpt[String].getOrElse("ED25519"),
          description = (json \ "description").asOpt[String].getOrElse("--"),
          isPublic = (json \ "is_public").asOpt[Boolean].getOrElse(false),
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
        format = BiscuitKeyPair.format,
        clazz = classOf[BiscuitKeyPair],
        keyf = id => datastores.biscuitKeyPairDataStore.key(id),
        extractIdf = c => datastores.biscuitKeyPairDataStore.extractId(c),
        extractIdJsonf = json => json.select("id").asString,
        idFieldNamef = () => "id",
        tmpl = (v, p) => {
          val biscuitKeyPair = new KeyPair()
          BiscuitKeyPair(
            id = IdGenerator.namedId("biscuit-keypair", env),
            name = "New Biscuit Key Pair",
            description = "New biscuit KeyPair",
            location = EntityLocation.default,
            privKey = biscuitKeyPair.toHex,
            pubKey = biscuitKeyPair.public_key().toHex
          ).json
        },
        canRead = true,
        canCreate = true,
        canUpdate = true,
        canDelete = true,
        canBulk = true,
        stateAll = () => states.allKeypairs(),
        stateOne = id => states.keypair(id),
        stateUpdate = values => states.updateKeyPairs(values)
      )
    )
  }
}

trait BiscuitKeyPairDataStore extends BasicStore[BiscuitKeyPair]

class KvBiscuitKeyPairDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitKeyPairDataStore
    with RedisLikeStore[BiscuitKeyPair] {
  override def fmt: Format[BiscuitKeyPair] = BiscuitKeyPair.format

  override def redisLike(implicit env: Env): RedisLike = redisCli

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:keypairs:$id"

  override def extractId(value: BiscuitKeyPair): String = value.id
}