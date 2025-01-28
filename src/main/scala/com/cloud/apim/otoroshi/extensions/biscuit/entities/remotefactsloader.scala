package com.cloud.apim.otoroshi.extensions.biscuit.entities

import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitRemoteUtils, BiscuitUtils}
import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models.{EntityLocation, EntityLocationSupport}
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.next.models.NgTlsConfig
import otoroshi.security.IdGenerator
import otoroshi.storage.{BasicStore, RedisLike, RedisLikeStore}
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionDatastores, BiscuitExtensionState}
import play.api.libs.json._
import otoroshi.utils.syntax.implicits._

import scala.concurrent.duration.{DurationInt, DurationLong, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class RemoteFactsData(
    acl: List[String] = List.empty,
    roles: List[String] = List.empty,
    facts: List[String] = List.empty,
    revoked: List[String] = List.empty
    ) {
  def json: JsValue = RemoteFactsData.format.writes(this)
}

object RemoteFactsData {
  val format = new Format[RemoteFactsData] {
    override def writes(o: RemoteFactsData): JsValue = {
      Json.obj(
        "acl" -> o.acl,
        "roles" -> o.roles,
        "facts" -> o.facts,
        "revoked" -> o.revoked
      )
    }

    override def reads(json: JsValue): JsResult[RemoteFactsData] =
      Try {
        RemoteFactsData(
           roles = (json \ "roles").asOpt[List[String]].getOrElse(List.empty),
           revoked = (json \ "revoked").asOpt[List[String]].getOrElse(List.empty),
           facts = (json \ "facts").asOpt[List[String]].getOrElse(List.empty),
           acl = (json \ "acl").asOpt[List[String]].getOrElse(List.empty),
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }
}

case class BiscuitRemoteFactsConfig(
                                     apiUrl: String = "",
                                     headers: Map[String, String] = Map.empty,
                                     tlsConfig: NgTlsConfig = NgTlsConfig(),
                                     timeout: FiniteDuration = 10.seconds
                                   ) {
  def json: JsValue = BiscuitRemoteFactsConfig.format.writes(this)
}

object BiscuitRemoteFactsConfig {
  val format = new Format[BiscuitRemoteFactsConfig] {
    override def writes(o: BiscuitRemoteFactsConfig): JsValue = {
      Json.obj(
        "api_url" -> o.apiUrl,
        "tls_config" -> o.tlsConfig.json,
        "headers" -> o.headers,
        "timeout" -> o.timeout.toMillis,
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitRemoteFactsConfig] =
      Try {
        BiscuitRemoteFactsConfig(
          apiUrl = json.select("api_url").asOpt[String].orElse(json.select("apiUrl").asOpt[String]).getOrElse(""),
          tlsConfig = json.select("tls_config").asOpt[JsObject].flatMap(o => NgTlsConfig.format.reads(o).asOpt).getOrElse(NgTlsConfig()),
          headers = json.select("headers").asOpt[Map[String, String]].getOrElse(Map.empty),
          timeout = json.select("timeout").asOpt[Long].map(_.millis).getOrElse(10.seconds),
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
                              config: BiscuitRemoteFactsConfig
                            ) extends EntityLocationSupport {
  def json: JsValue = RemoteFactsLoader.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags

  def loadFacts(ctx: JsValue)(implicit env: Env, ec: ExecutionContext): Future[Either[String, RemoteFactsData]] = {
    BiscuitRemoteUtils.getRemoteFacts(config, ctx).flatMap {
      case Left(err) => Left(s"unable to get remote facts ${err}").vfuture
      case Right(facts) => Right(facts).vfuture
    }
  }
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
        "config" -> o.config.json
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
          config = json.select("config").asOpt(BiscuitRemoteFactsConfig.format).getOrElse(BiscuitRemoteFactsConfig())
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
            )
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