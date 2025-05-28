package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit

import otoroshi.env.Env
import otoroshi.next.workflow.{WorkflowError, WorkflowFunction}
import otoroshi.utils.TypedMap
import otoroshi.utils.syntax.implicits._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

object WorkflowFunctionsInitializer {
  def initDefaults(): Unit = {
    WorkflowFunction.registerFunction("biscuit.extensions.cloud-apim.com.biscuit_verify", new BiscuitVerifyFunction())
    WorkflowFunction.registerFunction("biscuit.extensions.cloud-apim.com.biscuit_attenuation", new BiscuitAttenuationFunction())
    WorkflowFunction.registerFunction("biscuit.extensions.cloud-apim.com.biscuit_forge", new BiscuitForgeFunction())
  }
}

class BiscuitVerifyFunction extends WorkflowFunction {
  override def call(args: JsObject)(implicit env: Env, ec: ExecutionContext): Future[Either[WorkflowError, JsValue]] = {
    val verifier = args.select("verifier").asString
    val token = args.select("token").asString
    val extension = env.adminExtensions.extension[BiscuitExtension].get
    extension.states.biscuitVerifier(verifier) match {
      case None => WorkflowError(s"biscuit verifier not found", Some(Json.obj("verifier_id" -> verifier)), None).leftf
      case Some(verifier) => {
        verifier.verifyBase64Token(token, None, TypedMap.empty) // TODO: create verification context from attrs when otoroshi 17.3.0
          .map {
            case Left(error) => Json.obj("success" -> false, "error" -> error).right
            case Right(_) => Json.obj("success" -> true, "error" -> JsNull).right
          }
      }
    }
  }
}

class BiscuitAttenuationFunction extends WorkflowFunction {
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