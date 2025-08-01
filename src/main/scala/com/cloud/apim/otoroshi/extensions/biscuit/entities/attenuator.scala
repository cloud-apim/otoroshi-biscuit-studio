package com.cloud.apim.otoroshi.extensions.biscuit.entities

import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitUtils.handleBiscuitErrors
import org.biscuitsec.biscuit.token.Biscuit
import org.biscuitsec.biscuit.token.builder.parser.Parser
import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models.{EntityLocation, EntityLocationSupport}
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.security.IdGenerator
import otoroshi.storage.{BasicStore, RedisLike, RedisLikeStore}
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtension, BiscuitExtensionDatastores, BiscuitExtensionState}
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

case class AttenuatorConfig(
  checks: Seq[String] = Seq.empty
) {

  def json: JsValue = AttenuatorConfig.format.writes(this)

  def attenuate(biscuitToken: Biscuit)(implicit env: Env): Either[String, Biscuit] = {
    val block = biscuitToken.create_block()
    checks
      .map(_.trim.stripSuffix(";"))
      .map(Parser.check)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => block.add_check(r))

    Try(biscuitToken.attenuate(block)).toEither match {
      case Left(err: org.biscuitsec.biscuit.error.Error) =>
        Left(handleBiscuitErrors(err))
      case Left(err) =>
        Left(handleBiscuitErrors(new org.biscuitsec.biscuit.error.Error.InternalError()))
      case Right(biscuitToken) => Right(biscuitToken)
    }
  }
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
  config: AttenuatorConfig
) extends EntityLocationSupport {
  def json: JsValue = BiscuitAttenuator.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags

  def attenuate(biscuitToken: Biscuit)(implicit env: Env): Either[String, Biscuit] = {
    config.attenuate(biscuitToken)
  }

  def attenuateBase64Token(token: String)(implicit env: Env): Either[String, Biscuit] = {
    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keypairRef)) match {
      case None => Left("keypair_ref not found")
      case Some(keypair) => {
        Try(Biscuit.from_b64url(token, keypair.getPubKey)).toEither match {
          case Left(err) => Left(s"Unable to deserialize Biscuit token : ${err}")
          case Right(biscuitToken) => attenuate(biscuitToken)
        }
      }
    }
  }
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
        "config" -> o.config.json
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
          config = AttenuatorConfig.format.reads(json.select("config").asObject).get,
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
        tmpl = (v, p, ctx) => {
          BiscuitAttenuator(
            id = IdGenerator.namedId("biscuit-attenuator", env),
            name = "New biscuit Attenuator",
            description = "New biscuit Attenuator",
            location = EntityLocation.default,
            keypairRef = "",
            config = AttenuatorConfig()
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