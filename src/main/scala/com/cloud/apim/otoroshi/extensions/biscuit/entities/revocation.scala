package com.cloud.apim.otoroshi.extensions.biscuit.entities

import org.joda.time.DateTime
import otoroshi.env.Env
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import otoroshi.utils.syntax.implicits._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class RevokedToken(
  revocationId: String = "",
  reason: String = "unknown",
  revokedAt: DateTime = DateTime.now()
){
  def json: JsValue = RevokedToken.format.writes(this)
}
object RevokedToken {
  val format = new Format[RevokedToken] {
    override def writes(o: RevokedToken): JsValue = {
      Json.obj(
        "id" -> o.revocationId,
        "reason" -> o.reason,
        "revocation_date" -> o.revokedAt.toString(),
      )
    }

    override def reads(json: JsValue): JsResult[RevokedToken] =
      Try {
        RevokedToken(
          revocationId = (json \ "id").asOpt[String].getOrElse(""),
          reason = (json \ "reason").asOpt[String].getOrElse("unknown"),
          revokedAt = DateTime.parse(json.select("revocation_date").asOpt[String].getOrElse(DateTime.now().toString())),
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }
}

class RevocationDatastore()(implicit env: Env) {
  def list()(implicit ec: ExecutionContext): Future[Seq[RevokedToken]] = {
    val ext = env.adminExtensions.extension[BiscuitExtension].get
    val key = s"${env.storageRoot}:extensions:${ext.id.cleanup}:biscuit:revocation-list:*"
    env.datastores.rawDataStore.allMatching(key).map { seq =>
      seq.map(_.utf8String.parseJson.asObject).map(rvk => RevokedToken.format.reads(rvk).getOrElse(RevokedToken()))
    }
  }

  def exists(id: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    val ext = env.adminExtensions.extension[BiscuitExtension].get
    val key = s"${env.storageRoot}:extensions:${ext.id.cleanup}:biscuit:revocation-list:$id"
    env.datastores.rawDataStore.exists(key)
  }
  def existsAny(ids: Seq[String])(implicit ec: ExecutionContext): Future[Boolean] = {
    def next(remainingIds: Seq[String]): Future[Boolean] = {
      if (remainingIds.isEmpty) {
        Future(false)
      } else {
        val currentId = remainingIds.head
        exists(currentId).flatMap { existsResult =>
          if (existsResult) {
            Future(true)
          } else {
            next(remainingIds.tail)
          }
        }
      }
    }
    next(ids)
  }

  def add(id: String, reason: Option[String])(implicit ec: ExecutionContext): Future[Unit] = {
    val ext = env.adminExtensions.extension[BiscuitExtension].get
    val key = s"${env.storageRoot}:extensions:${ext.id.cleanup}:biscuit:revocation-list:$id"
    val theReason = reason.getOrElse("unknown")
    env.datastores.rawDataStore.set(
        key,
        Json.obj("id" -> id, "revocation_date" -> DateTime.now().toString(), "reason" -> theReason).stringify.byteString,
        None
    ).map(_ => ())
  }
}
