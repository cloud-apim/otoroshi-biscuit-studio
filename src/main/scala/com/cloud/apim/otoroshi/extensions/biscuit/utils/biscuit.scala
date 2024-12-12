package com.cloud.apim.otoroshi.extensions.biscuit.utils

import com.cloud.apim.otoroshi.extensions.biscuit.entities.VerifierConfig
import org.biscuitsec.biscuit.crypto._
import org.biscuitsec.biscuit.error.Error
import org.biscuitsec.biscuit.token.builder.Utils.{fact, string}
import org.biscuitsec.biscuit.token.builder.parser.Parser
import org.biscuitsec.biscuit.token.{Authorizer, Biscuit}
import otoroshi.env.Env
import otoroshi.plugins.biscuit.{BiscuitConfig, VerificationContext}
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Results}

import scala.jdk.CollectionConverters.{iterableAsScalaIterableConverter, seqAsJavaListConverter}
import scala.util.Try

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
        .replace("Biscuit ", "")
        .replace("biscuit: ", "")
        .replace("sealed-biscuit: ", "")
        .trim
      PubKeyBiscuitToken(tokenValue)
    }
  }

  def createToken(keyPair: KeyPair, facts: Seq[String]) : Biscuit = {
    return Biscuit.builder(keyPair)
      .build();
  }

  def attenuateToken(biscuitToken: Biscuit, checkConfig: Seq[String]) : Biscuit = {
    var block = biscuitToken.create_block()

     checkConfig
       .map(Parser.check)
       .filter(_.isRight)
       .map(_.get()._2)
       .foreach(r => block.add_check(r))

    return biscuitToken.attenuate(block);
  }

//  def sealToken(token: Biscuit) : Either[Error, Biscuit]  = {
//    val sealed_token = token.seal();
//  }

  def checkVerifierConfig(config: VerifierConfig)(implicit env: Env): Either[org.biscuitsec.biscuit.error.Error, Unit] = {
    val verifier = new Authorizer()

    try{
      // Resources
      config.resources.foreach(r => verifier.add_fact(s"""resource("${r}")"""))

      // Checks
      config.checks
        .map(Parser.check)
        .filter(_.isRight)
        .map(_.get()._2)
        .foreach(r => verifier.add_check(r))

      // Facts
      config.facts.map(Parser.fact).filter(_.isRight).map(_.get()._2).foreach(r => verifier.add_fact(r))

      // Rules
      config.rules
        .map(Parser.rule)
        .filter(_.isRight)
        .map(_.get()._2)
        .foreach(r => verifier.add_rule(r))

      env.logger.info(s"authorizer valide = ${verifier.print_world()}")
      Right()
    } catch {
      case e: Throwable => {
        e.printStackTrace()
        env.logger.info(s"authorizer PAS valide - ${e.getMessage}")
        Left(new Error.FormatError.DeserializationError("revoked token"))
      }
    }
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
    config.resources.foreach(r => verifier.add_fact(s"""resource("${r}")"""))

    // Checks
    config.checks
      .map(Parser.check)
      .filter(_.isRight)
      .map(_.get()._2)
      .foreach(r => verifier.add_check(r))

    // Facts
    config.facts.map(Parser.fact).filter(_.isRight).map(_.get()._2).foreach(r => verifier.add_fact(r))

    // Rules
    config.rules
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
      env.logger.info("token is revoked")
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