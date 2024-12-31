package com.cloud.apim.otoroshi.extensions.biscuit.plugins

import otoroshi.next.plugins.api.NgPluginConfig
import play.api.libs.json.{Format, JsError, JsNull, JsObject, JsResult, JsSuccess, JsValue, Json}
import otoroshi.utils.syntax.implicits._
import scala.util.{Failure, Success, Try}
case class BiscuitVerifierConfig(
      verifierRef: String,
      rbacPolicyRef: String,
      enforce: Boolean = true,
      extractorType: String,
      extractorName: String
) extends NgPluginConfig {
  def json: JsValue = BiscuitVerifierConfig.format.writes(this)
}

object BiscuitVerifierConfig {
  val configFlow: Seq[String] = Seq("verifier_ref", "rbac_ref", "enforce", "extractor_type", "extractor_name")
  def configSchema(name: String): Option[JsObject] = Some(Json.obj(
    "verifier_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"Biscuit Verifier",
      "props" -> Json.obj(
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/${name}",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    ),
    "rbac_ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"RBAC Policy Reference",
      "props" -> Json.obj(
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-rbac",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    ),
    "enforce" -> Json.obj(
      "type" -> "bool",
      "label" -> "Enforce"
    ),
    "extractor_type" -> Json.obj(
      "type" -> "select",
      "label" -> s"Extractor type",
      "props" -> Json.obj(
        "options" -> Seq(
          Json.obj("label" -> "Header", "value" -> "header"),
          Json.obj("label" -> "Cookies", "value" -> "cookies"),
          Json.obj("label" -> "Query params", "value" -> "query")
        )
      ),
    ),
    "extractor_name" -> Json.obj(
      "type" -> "text",
      "label" -> "Biscuit field name"
    )
  ))
  val default = BiscuitVerifierConfig(
    "",
    "",
    true,
    "Header",
    "Authorization"
  )

  val format = new Format[BiscuitVerifierConfig] {
    override def writes(o: BiscuitVerifierConfig): JsValue = Json.obj(
      "verifier_ref" -> o.verifierRef,
      "rbac_ref" -> o.rbacPolicyRef,
      "enforce" -> o.enforce,
      "extractor_type" -> o.extractorType,
      "extractor_name" -> o.extractorName,
    )
    override def reads(json: JsValue): JsResult[BiscuitVerifierConfig] = Try {
      BiscuitVerifierConfig(
        verifierRef = json.select("verifier_ref").asOpt[String].getOrElse(""),
        rbacPolicyRef = json.select("rbac_ref").asOpt[String].getOrElse(""),
        enforce = json.select("enforce").asOpt[Boolean].getOrElse(false),
        extractorType = json.select("extractor_type").asOpt[String].getOrElse(""),
        extractorName = json.select("extractor_name").asOpt[String].getOrElse("")
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }
}

case class BiscuitAttenuatorConfig(
    ref: String,
    extractorType: String,
    extractorName: String,
    tokenReplaceLoc: String,
    tokenReplaceName: String
) extends NgPluginConfig {
  def json: JsValue = BiscuitAttenuatorConfig.format.writes(this)
}

object BiscuitAttenuatorConfig {
  val configFlow: Seq[String] = Seq("ref", "extractor_type", "extractor_name", "token_replace_loc", "token_replace_name")
  def configSchema(name: String): Option[JsObject] = Some(Json.obj(
    "ref" -> Json.obj(
      "type" -> "select",
      "label" -> s"Biscuit Attenuator",
      "props" -> Json.obj(
        "optionsFrom" -> s"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/${name}",
        "optionsTransformer" -> Json.obj(
          "label" -> "name",
          "value" -> "id",
        ),
      ),
    ),
    "extractor_type" -> Json.obj(
      "type" -> "select",
      "label" -> s"Extractor type",
      "props" -> Json.obj(
        "options" -> Seq(
          Json.obj("label" -> "Header", "value" -> "header"),
          Json.obj("label" -> "Cookies", "value" -> "cookies"),
          Json.obj("label" -> "Query params", "value" -> "query")
        )
      ),
    ),
    "extractor_name" -> Json.obj(
      "type" -> "text",
      "label" -> "Biscuit field name"
    ),
    "token_replace_loc" -> Json.obj(
      "type" -> "select",
      "label" -> s"Replace location",
      "props" -> Json.obj(
        "options" -> Seq(
          Json.obj("label" -> "Header", "value" -> "header"),
          Json.obj("label" -> "Cookies", "value" -> "cookies"),
          Json.obj("label" -> "Query params", "value" -> "query")
        )
      ),
    ),
    "token_replace_name" -> Json.obj(
      "type" -> "text",
      "label" -> "New Biscuit field name"
    )
  ))

  val default = BiscuitAttenuatorConfig(
    "",
    "header",
    "Authorization",
    "header",
    "Authorization"
  )

  val format = new Format[BiscuitAttenuatorConfig] {
    override def writes(o: BiscuitAttenuatorConfig): JsValue = Json.obj(
      "ref" -> o.ref,
      "extractor_type" -> o.extractorType,
      "extractor_name" -> o.extractorName,
      "token_replace_loc" -> o.tokenReplaceLoc,
      "token_replace_name" -> o.tokenReplaceName,
    )
    override def reads(json: JsValue): JsResult[BiscuitAttenuatorConfig] = Try {
      BiscuitAttenuatorConfig(
        ref = json.select("ref").asOpt[String].getOrElse(""),
        extractorType = json.select("extractor_type").asOpt[String].getOrElse(""),
        extractorName = json.select("extractor_name").asOpt[String].getOrElse(""),
        tokenReplaceLoc = json.select("token_replace_loc").asOpt[String].getOrElse(""),
        tokenReplaceName = json.select("token_replace_name").asOpt[String].getOrElse(""),
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }
}