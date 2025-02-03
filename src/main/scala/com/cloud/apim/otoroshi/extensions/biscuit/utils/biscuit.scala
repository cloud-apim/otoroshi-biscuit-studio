package com.cloud.apim.otoroshi.extensions.biscuit.utils

import com.cloud.apim.otoroshi.extensions.biscuit.entities.{BiscuitRemoteFactsConfig, RemoteFactsData, VerifierConfig}
import org.biscuitsec.biscuit.crypto._
import org.biscuitsec.biscuit.datalog.RunLimits
import org.biscuitsec.biscuit.error.Error
import org.biscuitsec.biscuit.token.builder.Utils.{fact, string}
import org.biscuitsec.biscuit.token.builder.parser.Parser
import org.biscuitsec.biscuit.token.Biscuit
import org.biscuitsec.biscuit.token.builder.Block
import org.joda.time.DateTime
import otoroshi.env.Env
import otoroshi.plugins.biscuit.VerificationContext
import otoroshi.utils.http.RequestImplicits.EnhancedRequestHeader
import otoroshi.utils.syntax.implicits.BetterSyntax
import play.api.libs.json.{Format, JsError, JsResult, JsSuccess, JsValue, Json, Reads}
import play.api.mvc.RequestHeader

import scala.concurrent.{ExecutionContext, Future}
import java.security.SecureRandom
import scala.concurrent.duration.{DurationLong, FiniteDuration}
import scala.jdk.CollectionConverters.{iterableAsScalaIterableConverter, seqAsJavaListConverter}
import scala.util.{Failure, Success, Try}

case class BiscuitForgeConfig(
    checks: Seq[String] = Seq.empty,
    facts: Seq[String] = Seq.empty,
    resources: Seq[String] = Seq.empty,
    rules: Seq[String] = Seq.empty,
    enableTtl: Boolean = false,
    ttl: FiniteDuration = 1.hour
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
        "rules" -> o.rules,
        "enableTtl" -> o.enableTtl,
        "ttl" -> o.ttl.toMillis,
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitForgeConfig] =
      Try {
        BiscuitForgeConfig(
          checks = (json \ "checks").asOpt[Seq[String]].getOrElse(Seq.empty),
          facts = (json \ "facts").asOpt[Seq[String]].getOrElse(Seq.empty),
          resources = (json \ "resources").asOpt[Seq[String]].getOrElse(Seq.empty),
          rules = (json \ "rules").asOpt[Seq[String]].getOrElse(Seq.empty),
          enableTtl = (json \ "enableTtl").asOpt[Boolean].getOrElse(false),
          ttl = (json \ "ttl").asOpt[Long].map(_.millis).getOrElse(1.hour),
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

  def createToken(privKeyValue: String, forgeConfig: BiscuitForgeConfig): Biscuit = {

    val keypair = new KeyPair(privKeyValue)
    val rng = new SecureRandom()
    val authority_builder = new Block()

    val config = if (forgeConfig.enableTtl)
      forgeConfig.copy(
        checks = forgeConfig.checks :+
          s"check if time($$time), $$time <= ${DateTime.now().plusMillis(forgeConfig.ttl.toMillis.toInt)}"
      )
    else forgeConfig

    // Resources
    config.resources
      .map(_.trim.stripSuffix(";"))
      .foreach(r => authority_builder.add_fact(s"""resource("${r}")"""))

    // Checks
    config.checks
      .map(_.trim.stripSuffix(";"))
      .map(Parser.check)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => authority_builder.add_check(r))

    // Facts
    config.facts
      .map(_.trim.stripSuffix(";"))
      .map(Parser.fact)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => authority_builder.add_fact(r))

    // Rules
    config.rules
      .map(_.trim.stripSuffix(";"))
      .map(Parser.rule)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => authority_builder.add_rule(r))

    return Biscuit.make(rng, keypair, authority_builder.build())
  }

  def attenuateToken(biscuitToken: Biscuit, checkConfig: Seq[String]): Biscuit = {
    var block = biscuitToken.create_block()

    checkConfig
      .map(_.trim.stripSuffix(";"))
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
            )(implicit env: Env): Either[String, Unit] = {

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
      verifier.add_fact(fact("hostname", Seq(string(ctx.request.theHost)).asJava))
      verifier.add_fact(fact("resource", Seq(string(ctx.request.domain)).asJava))
      verifier.add_fact(fact("req_path", Seq(string(ctx.request.path)).asJava))
      verifier.add_fact(fact("req_domain", Seq(string(ctx.request.domain)).asJava))
      verifier.add_fact(fact("req_method", Seq(string(ctx.request.method.toLowerCase)).asJava))
      verifier.add_fact(fact("route_id", Seq(string(ctx.descriptor.id)).asJava))
      verifier.add_fact(fact("ip_address", Seq(string(ctx.request.theIpAddress)).asJava))
      verifier.add_fact(fact("user_agent", Seq(string(ctx.request.theUserAgent)).asJava))
      verifier.add_fact(fact("req_protocol", Seq(string(ctx.request.theProtocol)).asJava))

      if(ctx.user.isDefined){
        verifier.add_fact(fact("user", Seq(string(ctx.user.get.name)).asJava))
        verifier.add_fact(fact("email", Seq(string(ctx.user.get.email)).asJava))
      }

      if(ctx.apikey.isDefined){
        verifier.add_fact(fact("auth_method", Seq(string("apikey")).asJava))
      }

      ctx.request.headers.headers.map { case (headerName, headerValue) =>
        verifier.add_fact(fact(
          "req_headers",
          Seq(string(headerName.toLowerCase), string(headerValue)).asJava
        ))
      }
    }

    // Add resources from the configuration
    config.resources
      .map(_.trim.stripSuffix(";"))
      .foreach(r => verifier.add_fact(s"""resource("${r}")"""))

    // Checks
    config.checks
      .map(_.trim.stripSuffix(";"))
      .map(Parser.check)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_check(r))

    // Facts
    config.facts
      .map(_.trim.stripSuffix(";"))
      .map(Parser.fact)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_fact(r))

    // Rules
    config.rules
      .map(_.trim.stripSuffix(";"))
      .map(Parser.rule)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_rule(r))

    // Policies : allow or deny
    config.policies
      .map(_.trim.stripSuffix(";"))
      .map(Parser.policy)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_policy(r))

    // Check for token revocation
    val revokedIds = biscuitToken.revocation_identifiers().asScala.map(_.toHex).toList
    if (config.revokedIds.nonEmpty && config.revokedIds.exists(revokedIds.contains)) {
      return Left(handleBiscuitErrors(new Error.FormatError.DeserializationError("Revoked token")))
    }

    val maxFacts = 1000
    val maxIterations = 100
    val maxTime = java.time.Duration.ofMillis(100)

    env.logger.info(s"got verifier world = ${verifier.print_world()}")

    // Perform authorization
    if(verifier.policies().isEmpty){
      Try(verifier.allow().authorize(new RunLimits(maxFacts, maxIterations, maxTime))).toEither match {
        case Left(err: org.biscuitsec.biscuit.error.Error) =>
          Left(handleBiscuitErrors(err))
        case Left(err) =>
          Left(handleBiscuitErrors(new org.biscuitsec.biscuit.error.Error.InternalError()))
        case Right(_) =>
          Right(())
      }
    }else{
      Try(verifier.authorize(new RunLimits(maxFacts, maxIterations, maxTime))).toEither match {
        case Left(err: org.biscuitsec.biscuit.error.Error) =>
          Left(handleBiscuitErrors(err))
        case Left(err) =>
          Left(handleBiscuitErrors(new org.biscuitsec.biscuit.error.Error.InternalError()))
        case Right(_) =>
          Right(())
      }
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