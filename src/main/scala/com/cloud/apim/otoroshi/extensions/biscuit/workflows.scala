package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit

import biscuit.format.schema.Schema.PublicKey
import org.biscuitsec.biscuit.crypto.KeyPair
import otoroshi.env.Env
import otoroshi.next.workflow.{WorkflowError, WorkflowFunction, WorkflowRun}
import otoroshi.utils.syntax.implicits._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

object WorkflowFunctionsInitializer {
  def initDefaults(): Unit = {
    WorkflowFunction.registerFunction("extensions.cloud-apim.com.biscuit.biscuit_verify", new BiscuitVerifyFunction())
    WorkflowFunction.registerFunction("extensions.cloud-apim.com.biscuit.biscuit_attenuate", new BiscuitAttenuationFunction())
    WorkflowFunction.registerFunction("extensions.cloud-apim.com.biscuit.biscuit_forge", new BiscuitForgeFunction())
    WorkflowFunction.registerFunction("extensions.cloud-apim.com.biscuit.biscuit_keypair_generate", new BiscuitKeypairGenFunction())
  }
}

class BiscuitKeypairGenFunction extends WorkflowFunction {
  override def documentationName: String = "extensions.cloud-apim.com.biscuit.biscuit_keypair_gen"
  override def documentationDisplayName: String = "Biscuit keypair gen"
  override def documentationIcon: String = "fas fa-key"
  override def documentationDescription: String = "Generate a new biscuit keypair"
  override def documentationInputSchema: Option[JsObject] = Some(Json.obj(
    "type" -> "object",
    "required" -> Seq("alg"),
    "properties" -> Json.obj(
      "alg" -> Json.obj("type" -> "string", "description" -> "The algorithm to use for the keypair generation")
    )
  ))
  override def documentationFormSchema: Option[JsObject] = Some(Json.obj(
    "alg" -> Json.obj(
      "type"  -> "string",
      "label" -> "Algorithm",
      "props" -> Json.obj(
        "description" -> "The algorithm to use for the keypair generation"
      )
    )
  ))
  override def documentationCategory: Option[String] = Some("Cloud APIM - Biscuit extension")
  override def documentationOutputSchema: Option[JsObject] = Some(Json.obj(
    "type" -> "object",
    "properties" -> Json.obj(
      "public_key" -> Json.obj("type" -> "string", "description" -> "The public key"),
      "private_key" -> Json.obj("type" -> "string", "description" -> "The private key")
    )
  ))
  override def documentationExample: Option[JsObject] = Some(Json.obj(
    "kind" -> "call",
    "function" -> "extensions.cloud-apim.com.biscuit.biscuit_keypair_gen",
    "args" -> Json.obj(
      "alg" -> "ED25519"
    )
  ))
  override def callWithRun(args: JsObject)(implicit env: Env, ec: ExecutionContext, wfr: WorkflowRun): Future[Either[WorkflowError, JsValue]] = {
    val alg = args.select("alg").asOptString.getOrElse("ED25519").toLowerCase() match {
      case "ed25519" => PublicKey.Algorithm.Ed25519
      case _ => PublicKey.Algorithm.Ed25519
    }
    val kp = KeyPair.generate(alg)
    Json.obj(
      "public_key" -> kp.public_key().toHex.toUpperCase,
      "private_key" -> kp.toHex.toUpperCase,
    ).rightf
  }
}

class BiscuitVerifyFunction extends WorkflowFunction {
  override def documentationName: String = "extensions.cloud-apim.com.biscuit.biscuit_verify"
  override def documentationDisplayName: String = "Biscuit verify"
  override def documentationIcon: String = "fas fa-check"
  override def documentationDescription: String = "Verify a biscuit token"
  override def documentationInputSchema: Option[JsObject] = Some(Json.obj(
    "type" -> "object",
    "required" -> Seq("verifier", "token"),
    "properties" -> Json.obj(
      "verifier" -> Json.obj("type" -> "string", "description" -> "The verifier id"),
      "token" -> Json.obj("type" -> "string", "description" -> "The token to verify")
    )
  ))
  override def documentationFormSchema: Option[JsObject] = Some(Json.obj(
    "verifier" -> Json.obj(
      "type"  -> "string",
      "label" -> "Verifier id",
      "props" -> Json.obj(
        "description" -> "The verifier id"
      )
    ),
    "token" -> Json.obj(
      "type"  -> "string",
      "label" -> "Token",
      "props" -> Json.obj(
        "description" -> "The token to verify"
      )
    )
  ))
  override def documentationCategory: Option[String] = Some("Cloud APIM - Biscuit extension")
  override def documentationOutputSchema: Option[JsObject] = Some(Json.obj(
    "type" -> "object",
    "properties" -> Json.obj(
      "success" -> Json.obj("type" -> "boolean", "description" -> "The verification result"),
      "error" -> Json.obj("type" -> "string", "description" -> "The error message")
    )
  ))
  override def documentationExample: Option[JsObject] = Some(Json.obj(
    "kind" -> "call",
    "function" -> "extensions.cloud-apim.com.biscuit.biscuit_verify",
    "args" -> Json.obj(
      "verifier" -> "verifier_id",
      "token" -> "token"
    )
  ))
  override def callWithRun(args: JsObject)(implicit env: Env, ec: ExecutionContext, wfr: WorkflowRun): Future[Either[WorkflowError, JsValue]] = {
    val verifier = args.select("verifier").asString
    val token = args.select("token").asString
    val extension = env.adminExtensions.extension[BiscuitExtension].get
    extension.states.biscuitVerifier(verifier) match {
      case None => WorkflowError(s"biscuit verifier not found", Some(Json.obj("verifier_id" -> verifier)), None).leftf
      case Some(verifier) => {
        verifier.verifyBase64Token(token, None, wfr.attrs)
          .map {
            case Left(error) => Json.obj("success" -> false, "error" -> error).right
            case Right(_) => Json.obj("success" -> true, "error" -> JsNull).right
          }
      }
    }
  }
}

class BiscuitAttenuationFunction extends WorkflowFunction {
  override def documentationName: String = "extensions.cloud-apim.com.biscuit.biscuit_attenuation"
  override def documentationDisplayName: String = "Biscuit attenuation"
  override def documentationIcon: String = "fas fa-less-than"
  override def documentationDescription: String = "Attenuate a biscuit token"
  override def documentationInputSchema: Option[JsObject] = Some(Json.obj(
    "type" -> "object",
    "required" -> Seq("attenuator", "token"),
    "properties" -> Json.obj(
      "attenuator" -> Json.obj("type" -> "string", "description" -> "The attenuator id"),
      "token" -> Json.obj("type" -> "string", "description" -> "The token to attenuate")
    )
  ))
  override def documentationFormSchema: Option[JsObject] = Some(Json.obj(
    "attenuator" -> Json.obj(
      "type"  -> "string",
      "label" -> "Attenuator id",
      "props" -> Json.obj(
        "description" -> "The attenuator id"
      )
    ),
    "token" -> Json.obj(
      "type"  -> "string",
      "label" -> "Token",
      "props" -> Json.obj(
        "description" -> "The token to attenuate"
      )
    )
  ))
  override def documentationCategory: Option[String] = Some("Cloud APIM - Biscuit extension")
  override def documentationOutputSchema: Option[JsObject] = Some(Json.obj(
    "type" -> "object",
    "properties" -> Json.obj(
      "success" -> Json.obj("type" -> "boolean", "description" -> "The attenuation result"),
      "error" -> Json.obj("type" -> "string", "description" -> "The error message"),
      "biscuit_token" -> Json.obj("type" -> "string", "description" -> "The attenuated token")
    )
  ))
  override def documentationExample: Option[JsObject] = Some(Json.obj(
    "kind" -> "call",
    "function" -> "extensions.cloud-apim.com.biscuit.biscuit_attenuation",
    "args" -> Json.obj(
      "attenuator" -> "attenuator_id",
      "token" -> "token"
    )
  ))
  override def call(args: JsObject)(implicit env: Env, ec: ExecutionContext): Future[Either[WorkflowError, JsValue]] = {
    val attenuator = args.select("attenuator").asString
    val token = args.select("token").asString
    val extension = env.adminExtensions.extension[BiscuitExtension].get
    extension.states.biscuitAttenuator(attenuator) match {
      case None => WorkflowError(s"biscuit attenuator not found", Some(Json.obj("attenuator_id" -> attenuator)), None).leftf
      case Some(attenuator) => {
        attenuator.attenuateBase64Token(token) match {
          case Left(error) => Json.obj("success" -> false, "error" -> error, "biscuit_token" -> JsNull).rightf
          case Right(biscuit) => Json.obj("success" -> true, "error" -> JsNull, "biscuit_token" -> biscuit.serialize_b64url()).rightf
        }
      }
    }
  }
}

class BiscuitForgeFunction extends WorkflowFunction {
  override def documentationName: String = "extensions.cloud-apim.com.biscuit.biscuit_forge"
  override def documentationDisplayName: String = "Biscuit forge"
  override def documentationIcon: String = "fas fa-cookie"
  override def documentationDescription: String = "Forge a new biscuit token"
  override def documentationInputSchema: Option[JsObject] = Some(Json.obj(
    "type" -> "object",
    "required" -> Seq("forge"),
    "properties" -> Json.obj(
      "forge" -> Json.obj("type" -> "string", "description" -> "The forge id")
    )
  ))
  override def documentationFormSchema: Option[JsObject] = Some(Json.obj(
    "forge" -> Json.obj(
      "type"  -> "string",
      "label" -> "Forge id",
      "props" -> Json.obj(
        "description" -> "The forge id"
      )
    )
  ))
  override def documentationCategory: Option[String] = Some("Cloud APIM - Biscuit extension")
  override def documentationOutputSchema: Option[JsObject] = Some(Json.obj(
    "type" -> "object",
    "properties" -> Json.obj(
      "success" -> Json.obj("type" -> "boolean", "description" -> "The forging result"),
      "error" -> Json.obj("type" -> "string", "description" -> "The error message"),
      "biscuit_token" -> Json.obj("type" -> "string", "description" -> "The forged token")
    )
  ))
  override def documentationExample: Option[JsObject] = Some(Json.obj(
    "kind" -> "call",
    "function" -> "extensions.cloud-apim.com.biscuit.biscuit_forge",
    "args" -> Json.obj(
      "forge" -> "forge_id"
    )
  ))
  override def call(args: JsObject)(implicit env: Env, ec: ExecutionContext): Future[Either[WorkflowError, JsValue]] = {
    val forge = args.select("forge").asString
    val extension = env.adminExtensions.extension[BiscuitExtension].get
    extension.states.biscuitTokenForge(forge) match {
      case None => WorkflowError(s"biscuit forge not found", Some(Json.obj("forge_id" -> forge)), None).leftf
      case Some(forge) => {
        forge.forgeToken(Json.obj(), None) map {  // TODO: extract user from attrs
          case Left(error) => Json.obj("success" -> false, "error" -> error, "biscuit_token" -> JsNull).right
          case Right(biscuit) => Json.obj("success" -> true, "error" -> JsNull, "biscuit_token" -> biscuit.serialize_b64url()).right
        }
      }
    }
  }
}