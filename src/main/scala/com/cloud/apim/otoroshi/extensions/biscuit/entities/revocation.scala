package com.cloud.apim.otoroshi.extensions.biscuit.entities

import org.joda.time.DateTime
import otoroshi.env.Env
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.biscuitExtension
import otoroshi.utils.syntax.implicits.*
import play.api.libs.json.*

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

class RevocationDatastore()(using env: Env) {
  
  def list()(using ec: ExecutionContext): Future[Seq[RevokedToken]] = {
    val ext = env.biscuitExtension
    val key = s"${env.storageRoot}:extensions:${ext.id.cleanup}:biscuit:revocation-list:*"
    env.datastores.rawDataStore.allMatching(key).map { seq =>
      seq.map(_.utf8String.parseJson.asObject).map(rvk => RevokedToken.format.reads(rvk).getOrElse(RevokedToken()))
    }
  }

  def exists(id: String)(using ec: ExecutionContext): Future[Boolean] = {
    val ext = env.biscuitExtension
    val key = s"${env.storageRoot}:extensions:${ext.id.cleanup}:biscuit:revocation-list:$id"
    env.datastores.rawDataStore.exists(key)
  }

  def existsAny(ids: Seq[String])(using ec: ExecutionContext): Future[Boolean] = {
    // list().map { rtokens =>
    //   val lid = ULID.random()
    //   val revokedTokens = rtokens.map(_.revocationId)
    //   println(s"[${lid}] Trying to find the following revoked ids: " + ids.mkString(", "))
    //   println(s"[${lid}] Existing revoked ids: " + revokedTokens.mkString(", "))
    //   val found = revokedTokens.filter(i => ids.contains(i))
    //   println(s"[${lid}] found ${found.size}: ${found.mkString(", ")}")
    // }
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

  def add(id: String, reason: Option[String])(using ec: ExecutionContext): Future[Unit] = {
    val ext = env.biscuitExtension
    val key = s"${env.storageRoot}:extensions:${ext.id.cleanup}:biscuit:revocation-list:$id"
    val theReason = reason.getOrElse("unknown")
    env.datastores.rawDataStore.set(
        key,
        Json.obj("id" -> id, "revocation_date" -> DateTime.now().toString(), "reason" -> theReason).stringify.byteString,
        None
    ).map(_ => ())
  }
}
