package com.cloud.apim.otoroshi.extensions.biscuit.utils

import com.cloud.apim.otoroshi.extensions.biscuit.entities.VerifierConfig
import org.biscuitsec.biscuit.crypto._
import org.biscuitsec.biscuit.error.Error
import org.biscuitsec.biscuit.token.builder.Utils.{fact, string}
import org.biscuitsec.biscuit.token.builder.parser.Parser
import org.biscuitsec.biscuit.token.{Authorizer, Biscuit}
import org.biscuitsec.biscuit.token.builder.Block
import otoroshi.env.Env
import otoroshi.plugins.biscuit.VerificationContext
import play.api.libs.json.{Format, JsError, JsResult, JsSuccess, JsValue, Json, Reads}
import play.api.mvc.RequestHeader
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}
import java.security.SecureRandom
import scala.concurrent.Future
import scala.jdk.CollectionConverters.{iterableAsScalaIterableConverter, seqAsJavaListConverter}
import scala.util.{Failure, Success, Try}

case class BiscuitForgeConfig(
                               checks: Seq[String],
                               facts: Seq[String],
                               resources: Seq[String],
                               rules: Seq[String],
                             ) {
  def json: JsValue = BiscuitForgeConfig.format.writes(this)
}

object BiscuitForgeConfig {
  val format = new Format[BiscuitForgeConfig] {
    override def writes(o: BiscuitForgeConfig): JsValue = {
      Json.obj(
        "checks" -> o.checks,
        "facts" -> o.facts,
        "resources" -> o.resources,
        "rules" -> o.rules
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitForgeConfig] =
      Try {
        BiscuitForgeConfig(
          checks = (json \ "checks").asOpt[Seq[String]].getOrElse(Seq.empty),
          facts = (json \ "facts").asOpt[Seq[String]].getOrElse(Seq.empty),
          resources = (json \ "resources").asOpt[Seq[String]].getOrElse(Seq.empty),
          rules = (json \ "rules").asOpt[Seq[String]].getOrElse(Seq.empty),
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }
}

object BiscuitUtils {
  def extractToken(req: RequestHeader, extractorType: String, extractorName: String): Option[String] = {
    (extractorType match {
      case "header" => req.headers.get(extractorName)
      case "query" => req.getQueryString(extractorName)
      case "cookie" => req.cookies.get(extractorName).map(_.value)
      case _ => None
    }).map { token =>
      val tokenValue = token
        .replace("Bearer ", "")
        .replace("Bearer: ", "")
        .replace("Bearer:", "")
        .replace("Biscuit ", "")
        .replace("Biscuit-Token ", "")
        .replace("Biscuit-Token", "")
        .replace("BiscuitToken ", "")
        .replace("BiscuitToken", "")
        .replace("biscuit: ", "")
        .replace("biscuit:", "")
        .replace("sealed-biscuit: ", "")
        .replace("sealed-biscuit:", "")
        .trim
      tokenValue
    }
  }

  def createToken(privKeyValue: String, config: BiscuitForgeConfig): Biscuit = {

    val keypair = new KeyPair(privKeyValue)
    val rng = new SecureRandom()
    val authority_builder = new Block()

    // Resources
    config.resources
      .map(_.stripSuffix(";"))
      .foreach(r => authority_builder.add_fact(s"""resource("${r}")"""))

    // Checks
    config.checks
      .map(_.stripSuffix(";"))
      .map(Parser.check)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => authority_builder.add_check(r))

    // Facts
    config.facts
      .map(_.stripSuffix(";"))
      .map(Parser.fact)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => authority_builder.add_fact(r))

    // Rules
    config.rules
      .map(_.stripSuffix(";"))
      .map(Parser.rule)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => authority_builder.add_rule(r))

    return Biscuit.make(rng, keypair, authority_builder.build())
  }

  def attenuateToken(biscuitToken: Biscuit, checkConfig: Seq[String]): Biscuit = {
    var block = biscuitToken.create_block()

    checkConfig
      .map(_.stripSuffix(";"))
      .map(Parser.check)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => block.add_check(r))

    return biscuitToken.attenuate(block);
  }

  def verify(
              biscuitToken: Biscuit,
              config: VerifierConfig,
              ctxOpt: Option[VerificationContext]
            )(implicit env: Env): Either[org.biscuitsec.biscuit.error.Error, Unit] = {

    val verifier = biscuitToken.authorizer()
    verifier.set_time()

    if (ctxOpt.nonEmpty) {
      val ctx = ctxOpt.get

      verifier.add_fact(s"""operation("${readOrWrite(ctx.request.method)}")""")

      verifier.add_fact(
        fact(
          "resource",
          Seq(
            string(ctx.request.method.toLowerCase),
            string(ctx.request.domain),
            string(ctx.request.path)
          ).asJava
        )
      )
      verifier.add_fact(fact("resource", Seq(string(ctx.request.domain)).asJava))
      verifier.add_fact(fact("req_path", Seq(string(ctx.request.path)).asJava))
      verifier.add_fact(fact("req_domain", Seq(string(ctx.request.domain)).asJava))
      verifier.add_fact(fact("req_method", Seq(string(ctx.request.method.toLowerCase)).asJava))
      verifier.add_fact(fact("descriptor_id", Seq(string(ctx.descriptor.id)).asJava))

      ctx.apikey.foreach { apikey =>
        apikey.tags.foreach(tag => verifier.add_fact(fact("apikey_tag", Seq(string(tag)).asJava)))
        apikey.metadata.foreach { case (key, value) =>
          verifier.add_fact(fact("apikey_meta", Seq(string(key), string(value)).asJava))
        }
      }

      // Add user-related facts if available
      ctx.user.foreach { user =>
        user.metadata.foreach { case (key, value) =>
          verifier.add_fact(fact("user_meta", Seq(string(key), string(value)).asJava))
        }
      }

    }

    // Add resources from the configuration
    config.resources
      .map(_.stripSuffix(";"))
      .foreach(r => verifier.add_fact(s"""resource("${r}")"""))

    // Checks
    config.checks
      .map(_.stripSuffix(";"))
      .map(Parser.check)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_check(r))

    // Facts
    config.facts
      .map(_.stripSuffix(";"))
      .map(Parser.fact)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_fact(r))

    // Rules
    config.rules
      .map(_.stripSuffix(";"))
      .map(Parser.rule)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_rule(r))

    // Policies : allow or deny
    config.policies
      .map(_.stripSuffix(";"))
      .map(Parser.policy)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_policy(r))

    // Check for token revocation
    val revokedIds = biscuitToken.revocation_identifiers().asScala.map(_.toHex).toList
    if (config.revokedIds.nonEmpty && config.revokedIds.exists(revokedIds.contains)) {
      return Left(new Error.FormatError.DeserializationError("Revoked token"))
    }

    // Perform authorization
    Try(verifier.authorize()).toEither match {
      case Left(err: org.biscuitsec.biscuit.error.Error) =>
        Left(err)
      case Left(err) =>
        Left(new org.biscuitsec.biscuit.error.Error.InternalError())
      case Right(_) =>
        Right(())
    }
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
                      apiUrl: String,
                      headers: Map[String, String],
                    )(implicit env: Env, ec: ExecutionContext): Future[Either[String, (List[String], List[String], List[String], List[String])]] = {

    env.Ws
      .url(apiUrl)
      .withHttpHeaders(
        headers.toSeq: _*
      )
      .withRequestTimeout(scala.concurrent.duration.Duration("10s"))
      .withMethod("GET")
      .execute()
      .map { resp =>
        resp.status match {
          case 200 =>
            val rolesResult = (resp.json \ "roles").validate[List[Map[String, List[String]]]].getOrElse(List.empty)
            val revokedResult = (resp.json \ "revoked").validate[List[String]].getOrElse(List.empty)
            val factsResult = (resp.json \ "facts").validate[List[Map[String, String]]].getOrElse(List.empty)
            val aclResult = (resp.json \ "acl").validate[List[Map[String, String]]].getOrElse(List.empty)
            val userRolesResult = (resp.json \ "user_roles").validate[List[Map[String, JsValue]]].getOrElse(List.empty)

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

            Right((roleFacts ++ userRoleFacts, revokedIdsRemote, factsStrings, aclStrings))

          case _ =>
            Left(s"API request failed with status ${resp.status}: ${resp.body}")
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