package com.cloud.apim.otoroshi.extensions.biscuit.utils

import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitForgeConfig, BiscuitRemoteFactsConfig, RemoteFactsData}
import org.biscuitsec.biscuit.crypto._
import org.biscuitsec.biscuit.token.Biscuit
import org.biscuitsec.biscuit.token.builder.Block
import org.biscuitsec.biscuit.token.builder.parser.Parser
import org.joda.time.DateTime
import otoroshi.env.Env
import otoroshi.utils.syntax.implicits.BetterSyntax
import play.api.libs.json._
import play.api.mvc.RequestHeader

import java.security.SecureRandom
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
object BiscuitUtils {

  def extractToken(req: RequestHeader, extractorType: String, extractorName: String): Option[String] = {
    (extractorType match {
      case "header" => req.headers.get(extractorName)
      case "query" => req.getQueryString(extractorName)
      case "cookie" => req.cookies.get(extractorName).map(_.value)
      case _ => None
    }).map { token =>
      replaceHeader(token)
    }
  }

  def replaceHeader(token: String): String = {
    token
      .replace("Bearer ", "")
      .replace("Bearer: ", "")
      .replace("Bearer:", "")
      .replace("Biscuit ", "")
      .replace("Biscuit: ", "")
      .replace("Biscuit-Token ", "")
      .replace("Biscuit-Token", "")
      .replace("BiscuitToken ", "")
      .replace("BiscuitToken", "")
      .replace("biscuit: ", "")
      .replace("biscuit:", "")
      .replace("sealed-biscuit: ", "")
      .replace("sealed-biscuit:", "")
      .trim
  }

  def readOrWrite(method: String): String =
    method match {
      case "DELETE" => "write"
      case "GET" => "read"
      case "HEAD" => "read"
      case "OPTIONS" => "read"
      case "PATCH" => "write"
      case "POST" => "write"
      case "PUT" => "write"
      case _ => "none"
    }


  def handleBiscuitErrors(error: org.biscuitsec.biscuit.error.Error)(implicit env: Env): String = {
    error match {
      case err: org.biscuitsec.biscuit.error.Error.FormatError.UnknownPublicKey => {
        s"UnknownPublicKey"
      }

      case err: org.biscuitsec.biscuit.error.Error.FormatError.DeserializationError => {
        s"DeserializationError - ${err.e}"
      }

      case err: org.biscuitsec.biscuit.error.Error.FailedLogic => {
        s"FailedLogic - ${err.error.toString}"
      }

      case err: org.biscuitsec.biscuit.error.Error.InvalidAuthorityIndex => {
        s"InvalidAuthorityIndex - ${err.index}"
      }

      case err: org.biscuitsec.biscuit.error.Error.InvalidBlockIndex => {
        s"InvalidBlockIndex - expected:  ${err.expected} found: ${err.found}"
      }

      case err: org.biscuitsec.biscuit.error.Error.MissingSymbols => {
        s"Biscuit MissingSymbols"
      }

      case err: org.biscuitsec.biscuit.error.Error.Timeout => {
        s"Biscuit Timeout"
      }

      case err: org.biscuitsec.biscuit.error.Error.InvalidType => {
        s"Biscuit InvalidType"
      }

      case err: org.biscuitsec.biscuit.error.Error.InternalError => {
        "Biscuit InternalError"
      }

      case _ => {
        error.toString
      }
    }
  }
}

object BiscuitRemoteUtils {

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


  def getRemoteFacts(
                      config: BiscuitRemoteFactsConfig,
                      ctx: JsValue,
                    )(implicit env: Env, ec: ExecutionContext): Future[Either[String, RemoteFactsData]] = {
    env.MtlsWs
      .url(config.apiUrl, config.tlsConfig.legacy)
      .withHttpHeaders(
        config.headers.toSeq: _*
      )
      .withMethod(config.method)
      .applyOnIf(config.method == "POST" || config.method == "PUT" || config.method == "PATCH") { builder =>
        builder.withBody(Json.obj("context" -> ctx))
      }
      .withRequestTimeout(config.timeout)
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
              s"""role("${role.name}", [${role.permissions.map(p => s""""$p"""").mkString(", ")}]);"""
            }

            val userRoleFacts = userRoles.map { userRole =>
              s"""user_roles(${userRole.id}, "${userRole.name}", [${userRole.roles.map(r => s""""$r"""").mkString(", ")}]);"""
            }


            val revokedIdsRemote = revokedIds.map(_.id)
            val factsStrings = facts.map(fact => s"""${fact.name}("${fact.value}")""")
            val aclStrings = aclEntries.map(acl => s"""right("${acl.user}", "${acl.resource}", "${acl.action}");""")


            val rfd = RemoteFactsData(
              acl = aclStrings,
              roles = roleFacts ++ userRoleFacts,
              facts = factsStrings,
              revoked = revokedIdsRemote,
              checks = checksResult
            )

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

  case class Role(name: String, permissions: List[String])

  case class BiscuitrevokedId(id: String)

  case class Fact(name: String, value: String)

  case class ACL(user: String, resource: String, action: String)

  case class UserRole(id: Int, name: String, roles: List[String])
}