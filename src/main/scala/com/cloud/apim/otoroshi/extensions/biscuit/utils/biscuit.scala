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
import play.api.libs.json.{Format, JsError, JsResult, JsSuccess, JsValue, Json}
import play.api.mvc.RequestHeader

import java.security.SecureRandom
import scala.jdk.CollectionConverters.{iterableAsScalaIterableConverter, seqAsJavaListConverter}
import scala.util.{Failure, Success, Try}

case class BiscuitForgeConfig(
                           checks: Seq[String],
                           facts: Seq[String],
                           resources: Seq[String],
                           rules: Seq[String],
                         ){
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

sealed trait BiscuitToken {
  def token: String
}
case class PubKeyBiscuitToken(token: String) extends BiscuitToken
case class SealedBiscuitToken(token: String) extends BiscuitToken
object BiscuitUtils {
  def readOrWrite(method: String): String =
    method match {
      case "DELETE"  => "write"
      case "GET"     => "read"
      case "HEAD"    => "read"
      case "OPTIONS" => "read"
      case "PATCH"   => "write"
      case "POST"    => "write"
      case "PUT"     => "write"
      case _         => "none"
    }

  def extractToken(req: RequestHeader, extractorType: String, extractorName: String): Option[BiscuitToken] = {
    (extractorType match {
      case "header" => req.headers.get(extractorName)
      case "query"  => req.getQueryString(extractorName)
      case "cookie" => req.cookies.get(extractorName).map(_.value)
      case _        => None
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
      PubKeyBiscuitToken(tokenValue)
    }
  }

  def createToken(privKeyValue: String, config: BiscuitForgeConfig) : Biscuit = {

    val keypair      = new KeyPair(privKeyValue)
    val rng          = new SecureRandom()
    val authority_builder = new Block()

    // Resources
    config.resources.foreach(r => authority_builder.add_fact(s"""resource("${r}")"""))

    // Checks
    config.checks
      .map(_.stripSuffix(";"))
      .map(Parser.check)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => authority_builder.add_check(r))

    // Facts
    config.facts.map(Parser.fact).filter(_.isRight).map(_.get()._2).foreach(r => authority_builder.add_fact(r))

    // Rules
    config.rules
      .map(_.stripSuffix(";"))
      .map(Parser.rule)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => authority_builder.add_rule(r))

    return Biscuit.make(rng, keypair, authority_builder.build())
  }

  def attenuateToken(biscuitToken: Biscuit, checkConfig: Seq[String]) : Biscuit = {
    var block = biscuitToken.create_block()

     checkConfig
       .map(_.stripSuffix(";"))
       .map(Parser.check)
       .filter(_.isRight)
       .map(_.get()._2)
       .foreach(r => block.add_check(r))

    return biscuitToken.attenuate(block);
  }

  def verify(biscuitToken: Biscuit, config: VerifierConfig, ctx: VerificationContext)(implicit
                                                                                    env: Env
  ): Either[org.biscuitsec.biscuit.error.Error, Unit] = {

    val verifier = biscuitToken.authorizer()
    verifier.set_time()
    verifier.add_fact(s"""operation("${readOrWrite(ctx.request.method)}")""")
    verifier.add_fact(
      fact(
        "resource",
        Seq(
          string(ctx.request.method.toLowerCase()),
          string(ctx.request.domain),
          string(ctx.request.path)
        ).asJava
      )
    )
    verifier.add_fact(fact("req_path", Seq(string(ctx.request.path)).asJava))
    verifier.add_fact(fact("req_domain", Seq(string(ctx.request.domain)).asJava))
    verifier.add_fact(fact("req_method", Seq(string(ctx.request.method.toLowerCase())).asJava))
    verifier.add_fact(fact("descriptor_id", Seq(string(ctx.descriptor.id)).asJava))
    ctx.apikey.foreach { apikey =>
      apikey.tags.foreach(tag => verifier.add_fact(fact("apikey_tag", Seq(string(tag)).asJava)))
      apikey.metadata.foreach(tuple =>
        verifier.add_fact(fact("apikey_meta", Seq(string(tuple._1), string(tuple._2)).asJava))
      )
    }
    ctx.user.foreach { user =>
      user.metadata.foreach(tuple =>
        verifier.add_fact(fact("user_meta", Seq(string(tuple._1), string(tuple._2)).asJava))
      )
    }

    // Resources
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


    val recocationIds =
      biscuitToken.revocation_identifiers()
        .asScala
        .map(r => r.toHex)
        .toList

    if (config.revocation_ids.nonEmpty && config.revocation_ids.exists(id => recocationIds.contains(id))) {
      Left(new Error.FormatError.DeserializationError("revoked token"))
    } else {
      // TODO: here, add rules from config, query some stuff, etc ..
      Try(verifier.allow().authorize()).toEither match {
        case Left(err: org.biscuitsec.biscuit.error.Error) => Left(err)
        case Left(err)                                     => Left(new org.biscuitsec.biscuit.error.Error.InternalError())
        case Right(_)                                      => Right(())
      }
    }
  }
}