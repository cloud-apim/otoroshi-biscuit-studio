package com.cloud.apim.otoroshi.extensions.biscuit.plugins

import otoroshi.next.plugins.api.NgPluginConfig
import play.api.libs.json.{Format, JsError, JsNull, JsObject, JsResult, JsSuccess, JsValue, Json}
import otoroshi.utils.syntax.implicits._
import scala.util.{Failure, Success, Try}
case class BiscuitVerifierConfig(
      verifierRef: String,
      enforce: Boolean,
      extractorType: String,
      extractorName: String
) extends NgPluginConfig {
  def json: JsValue = BiscuitVerifierConfig.format.writes(this)
}

object BiscuitVerifierConfig {
  val configFlow: Seq[String] = Seq("verifier_ref", "enforce", "extractor_type", "extractor_name")
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
    false,
    "Header",
    "Authorization"
  )

  val format = new Format[BiscuitVerifierConfig] {
    override def writes(o: BiscuitVerifierConfig): JsValue = Json.obj(
      "verifier_ref" -> o.verifierRef,
      "enforce" -> o.enforce,
      "extractor_type" -> o.extractorType,
      "extractor_name" -> o.extractorName,
    )
    override def reads(json: JsValue): JsResult[BiscuitVerifierConfig] = Try {
      BiscuitVerifierConfig(
        verifierRef = json.select("verifier_ref").asOpt[String].getOrElse(""),
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
    extractorName: String
) extends NgPluginConfig {
  def json: JsValue = BiscuitAttenuatorConfig.format.writes(this)
}

object BiscuitAttenuatorConfig {
  val configFlow: Seq[String] = Seq("ref", "extractor_type", "extractor_name")
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
    )
  ))

  val default = BiscuitAttenuatorConfig(
    "",
    "Header",
    "Authorization"
  )

  val format = new Format[BiscuitAttenuatorConfig] {
    override def writes(o: BiscuitAttenuatorConfig): JsValue = Json.obj(
      "ref" -> o.ref,
      "extractor_type" -> o.extractorType,
      "extractor_name" -> o.extractorName,
    )
    override def reads(json: JsValue): JsResult[BiscuitAttenuatorConfig] = Try {
      BiscuitAttenuatorConfig(
        ref = json.select("ref").asOpt[String].getOrElse(""),
        extractorType = json.select("extractor_type").asOpt[String].getOrElse(""),
        extractorName = json.select("extractor_name").asOpt[String].getOrElse("")
      )
    } match {
      case Failure(exception) => JsError(exception.getMessage)
      case Success(value) => JsSuccess(value)
    }
  }
}