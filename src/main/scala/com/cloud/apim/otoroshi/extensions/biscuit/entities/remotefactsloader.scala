package com.cloud.apim.otoroshi.extensions.biscuit.entities

import akka.util.ByteString
import com.github.blemale.scaffeine.Scaffeine
import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models.{EntityLocation, EntityLocationSupport}
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.next.models.NgTlsConfig
import otoroshi.security.IdGenerator
import otoroshi.storage.{BasicStore, RedisLike, RedisLikeStore}
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtensionDatastores, BiscuitExtensionState}
import play.api.libs.json._

import scala.concurrent.duration.{DurationInt, DurationLong, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object RemoteReads {

  implicit val rolesReads: Reads[List[Role]] = Reads { json =>
    (json \ "roles").validateOpt[List[Map[String, List[String]]]].map {
      case Some(roles) =>
        roles.flatMap(_.toList.map { case (name, permissions) =>
          Role(name, permissions)
        })
      case None => List.empty
    }
  }

  implicit val revokedIdsReads: Reads[List[BiscuitrevokedId]] = Reads { json =>
    (json \ "revoked").validateOpt[List[String]].map {
      case Some(ids) => ids.map(BiscuitrevokedId)
      case None => List.empty
    }
  }

  implicit val factsReads: Reads[List[Fact]] = Reads { json =>
    (json \ "facts").validateOpt[List[Map[String, String]]].map {
      case Some(facts) =>
        facts.flatMap { fact =>
          for {
            name <- fact.get("name")
            value <- fact.get("value")
          } yield Fact(name, value)
        }
      case None => List.empty
    }
  }

  implicit val aclReads: Reads[List[ACL]] = Reads { json =>
    (json \ "acl").validateOpt[List[Map[String, String]]].map {
      case Some(acls) =>
        acls.flatMap { acl =>
          for {
            user <- acl.get("user")
            resource <- acl.get("resource")
            action <- acl.get("action")
          } yield ACL(user, resource, action)
        }
      case None => List.empty
    }
  }

  implicit val userRolesReads: Reads[List[UserRole]] = Reads { json =>
    (json \ "user_roles").validateOpt[List[Map[String, JsValue]]].map {
      case Some(userRoles) =>
        userRoles.flatMap { userRole =>
          for {
            id <- userRole.get("id").flatMap(_.asOpt[Int])
            name <- userRole.get("name").flatMap(_.asOpt[String])
            roles <- userRole.get("roles").flatMap(_.asOpt[List[String]])
          } yield UserRole(id, name, roles)
        }
      case None => List.empty
    }
  }
}

case class Role(name: String, permissions: List[String])

case class BiscuitrevokedId(id: String)

case class Fact(name: String, value: String)

case class ACL(user: String, resource: String, action: String)

case class UserRole(id: Int, name: String, roles: List[String])

case class RemoteFactsData(
    acl: List[String] = List.empty,
    roles: List[String] = List.empty,
    facts: List[String] = List.empty,
    revoked: List[String] = List.empty,
    checks: List[String] = List.empty
) {
  def json: JsValue = RemoteFactsData.format.writes(this)
  def merge(other: RemoteFactsData): RemoteFactsData = {
    RemoteFactsData(
      acl = acl ++ other.acl,
      roles = roles ++ other.roles,
      facts = facts ++ other.facts,
      revoked = revoked ++ other.revoked,
      checks = checks ++ other.checks,
    )
  }
}

object RemoteFactsData {
  val format = new Format[RemoteFactsData] {
    override def writes(o: RemoteFactsData): JsValue = {
      Json.obj(
        "acl" -> o.acl,
        "roles" -> o.roles,
        "facts" -> o.facts,
        "revoked" -> o.revoked,
        "checks" -> o.checks
      )
    }

    override def reads(json: JsValue): JsResult[RemoteFactsData] =
      Try {
        RemoteFactsData(
           roles = (json \ "roles").asOpt[List[String]].getOrElse(List.empty),
           revoked = (json \ "revoked").asOpt[List[String]].getOrElse(List.empty),
           facts = (json \ "facts").asOpt[List[String]].getOrElse(List.empty),
           acl = (json \ "acl").asOpt[List[String]].getOrElse(List.empty),
           checks = (json \ "checks").asOpt[List[String]].getOrElse(List.empty),
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }
}



case class BiscuitRemoteFactsConfig(
  apiUrl: String = "",
  method: String = "POST",
  headers: Map[String, String] = Map.empty,
  tlsConfig: NgTlsConfig = NgTlsConfig(),
  timeout: FiniteDuration = 10.seconds
) {

  def json: JsValue = BiscuitRemoteFactsConfig.format.writes(this)

  def getRemoteFacts(ctx: JsValue)(implicit env: Env, ec: ExecutionContext): Future[Either[String, RemoteFactsData]] = {
    val withBody = method == "POST" || method == "PUT" || method == "PATCH"
    val key = apiUrl // TODO: find a way to cache even with context

    def call(): Future[Either[String, RemoteFactsData]] = {
      env.MtlsWs
        .url(apiUrl, tlsConfig.legacy)
        .withHttpHeaders(
          headers.toSeq: _*
        )
        .withMethod(method)
        .applyOnIf(withBody) { builder =>
          builder.withBody(Json.obj("context" -> ctx))
        }
        .withRequestTimeout(timeout)
        .execute()
        .map { resp =>
          resp.status match {
            case 200 =>
              val rolesResult = (resp.json \ "roles").validate[List[Map[String, List[String]]]].getOrElse(List.empty)
              val revokedResult = (resp.json \ "revoked").validate[List[String]].getOrElse(List.empty)
              val factsResult = (resp.json \ "facts").validate[List[Map[String, String]]].getOrElse(List.empty)
              val aclResult = (resp.json \ "acl").validate[List[Map[String, String]]].getOrElse(List.empty)
              val userRolesResult = (resp.json \ "user_roles").validate[List[Map[String, JsValue]]].getOrElse(List.empty)
              val checksResult = (resp.json \ "checks").validate[List[String]].getOrElse(List.empty)


              val roles = rolesResult.flatMap(_.toList.map { case (name, permissions) =>
                Role(name, permissions)
              })

              val revokedIds = revokedResult.map(BiscuitrevokedId)

              val facts = factsResult.flatMap { fact =>
                for {
                  name <- fact.get("name")
                  value <- fact.get("value")
                } yield Fact(name, value)
              }

              val aclEntries = aclResult.flatMap { acl =>
                for {
                  user <- acl.get("user")
                  resource <- acl.get("resource")
                  action <- acl.get("action")
                } yield ACL(user, resource, action)
              }

              val userRoles = userRolesResult.flatMap { userRole =>
                for {
                  id <- userRole.get("id").flatMap(_.asOpt[Int])
                  name <- userRole.get("name").flatMap(_.asOpt[String])
                  roles <- userRole.get("roles").flatMap(_.asOpt[List[String]])
                } yield UserRole(id, name, roles)
              }

              val roleFacts = roles.map { role =>
                s"""role("${role.name}", [${role.permissions.map(p => s""""$p"""").mkString(", ")}])"""
              }

              val userRoleFacts = userRoles.map { userRole =>
                s"""user_roles(${userRole.id}, "${userRole.name}", [${userRole.roles.map(r => s""""$r"""").mkString(", ")}])"""
              }


              val revokedIdsRemote = revokedIds.map(_.id)
              val factsStrings = facts.map(fact => s"""${fact.name}("${fact.value}")""")
              val aclStrings = aclEntries.map(acl => s"""right("${acl.user}", "${acl.resource}", "${acl.action}")""")

              val rfd = RemoteFactsData(
                acl = aclStrings,
                roles = roleFacts ++ userRoleFacts,
                facts = factsStrings,
                revoked = revokedIdsRemote,
                checks = checksResult
              )

              if (withBody) {
                BiscuitRemoteFactsConfig.cache.put(key, rfd)
              }

              Right(rfd)

            case _ =>
              Left(s"API request failed with status ${resp.status} (${resp.statusText})")
          }
        }
        .recover {
          case ex: Exception =>
            Left(s"An error occurred during API request: ${ex.getMessage}")
        }
    }

    if (withBody) {
      call()
    } else {
      BiscuitRemoteFactsConfig.cache.getIfPresent(key) match {
        case Some(data) => data.rightf
        case None => call()
      }
    }
  }

}

object BiscuitRemoteFactsConfig {
  val cache = Scaffeine().maximumSize(10000).build[String, RemoteFactsData]
  val format = new Format[BiscuitRemoteFactsConfig] {
    override def writes(o: BiscuitRemoteFactsConfig): JsValue = {
      Json.obj(
        "api_url" -> o.apiUrl,
        "tls_config" -> o.tlsConfig.json,
        "headers" -> o.headers,
        "method" -> o.method,
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
          method = json.select("method").asOpt[String].getOrElse("POST"),
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
    config.getRemoteFacts(ctx).flatMap {
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