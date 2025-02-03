package com.cloud.apim.otoroshi.extensions.biscuit.entities

import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitForgeConfig, BiscuitUtils}
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models._
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.security.IdGenerator
import otoroshi.storage._
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtension, BiscuitExtensionDatastores, BiscuitExtensionState}
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class BiscuitTokenForge(
                              id: String,
                              name: String,
                              description: String,
                              keypairRef: String,
                              config: BiscuitForgeConfig,
                              tags: Seq[String],
                              metadata: Map[String, String],
                              location: EntityLocation,
                              remoteFactsLoaderRef: Option[String]
                            ) extends EntityLocationSupport {
  def json: JsValue = BiscuitTokenForge.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags

  def forgeToken(ctx: JsValue)(implicit env: Env, ec: ExecutionContext): Future[Either[String, Biscuit]] = {
    env.adminExtensions.extension[BiscuitExtension].get.states.keypair(keypairRef) match {
      case None => Left("keypair not found").vfuture
      case Some(kp) => {
        remoteFactsLoaderRef match {
          case None => {
            BiscuitUtils.createToken(kp.privKey, config) match {
              case Left(err) => Left("unable to forge token").vfuture
              case Right(token) => Right(token).vfuture
            }
          }
          case Some(remoteFactsRef) => {
            env.adminExtensions.extension[BiscuitExtension].get.states.biscuitRemoteFactsLoader(remoteFactsRef) match {
              case None => Left("remote facts reference not found").vfuture
              case Some(remoteFacts) => {
                remoteFacts.loadFacts(ctx).flatMap {
                  case Left(error) => Left(error).vfuture
                  case Right(remoteFacts) => {

                    val finalConfig = config.copy(
                      facts = remoteFacts.facts ++ remoteFacts.acl ++ remoteFacts.roles,
                    )

                    BiscuitUtils.createToken(kp.privKey, finalConfig) match {
                      case Left(err) => Left("unable to forge token").vfuture
                      case Right(token) => Right(token).vfuture
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
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
        "config" -> o.config.json,
        "tags" -> JsArray(o.tags.map(JsString.apply)),
        "remoteFactsLoaderRef" -> o.remoteFactsLoaderRef
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
          metadata = (json \ "metadata").asOpt[Map[String, String]].getOrElse(Map.empty),
          tags = (json \ "tags").asOpt[Seq[String]].getOrElse(Seq.empty[String]),
          config = json.select("config").asOpt(BiscuitForgeConfig.format).getOrElse(BiscuitForgeConfig()),
          remoteFactsLoaderRef = json.select("remoteFactsLoaderRef").asOpt[String]
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }

  def resource(env: Env, datastores: BiscuitExtensionDatastores, states: BiscuitExtensionState): Resource = {
    Resource(
      "BiscuitForge",
      "biscuit-forges",
      "biscuit-forge",
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
            id = IdGenerator.namedId("biscuit-forge", env),
            name = "New biscuit forge",
            description = "New biscuit forge",
            keypairRef = "",
            metadata = Map.empty,
            tags = Seq.empty,
            location = EntityLocation.default,
            remoteFactsLoaderRef = None,
            config = BiscuitForgeConfig()
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

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:forge:$id"

  override def extractId(value: BiscuitTokenForge): String = value.id
}