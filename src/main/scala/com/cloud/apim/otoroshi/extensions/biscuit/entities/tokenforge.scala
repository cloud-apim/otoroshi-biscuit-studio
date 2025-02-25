package com.cloud.apim.otoroshi.extensions.biscuit.entities

import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitUtils
import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitUtils.handleBiscuitErrors
import org.biscuitsec.biscuit.crypto.KeyPair
import org.biscuitsec.biscuit.token.Biscuit
import org.biscuitsec.biscuit.token.builder.Block
import org.biscuitsec.biscuit.token.builder.parser.Parser
import org.joda.time.DateTime
import otoroshi.api.{GenericResourceAccessApiWithState, Resource, ResourceVersion}
import otoroshi.env.Env
import otoroshi.models._
import otoroshi.next.extensions.AdminExtensionId
import otoroshi.security.IdGenerator
import otoroshi.storage._
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.{BiscuitExtension, BiscuitExtensionDatastores, BiscuitExtensionState}
import play.api.libs.json._

import java.security.SecureRandom
import scala.concurrent.duration.{DurationLong, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
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

  def createToken(privKeyValue: String)(implicit env: Env): Either[String, Biscuit] = {

    val keypair = new KeyPair(privKeyValue)
    val rng = new SecureRandom()
    val authority_builder = new Block()

    val config = if (enableTtl)
      copy(
        checks = checks :+
          s"check if time($$time), $$time <= ${DateTime.now().plusMillis(ttl.toMillis.toInt)}"
      )
    else this

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

    Try(Biscuit.make(rng, keypair, authority_builder.build())).toEither match {
      case Left(err: org.biscuitsec.biscuit.error.Error) =>
        Left(handleBiscuitErrors(err))
      case Left(err) =>
        Left(handleBiscuitErrors(new org.biscuitsec.biscuit.error.Error.InternalError()))
      case Right(biscuitToken) => Right(biscuitToken)
    }
  }
}

object BiscuitForgeConfig {
  val format = new Format[BiscuitForgeConfig] {
    override def writes(o: BiscuitForgeConfig): JsValue = {
      Json.obj(
        "checks" -> o.checks,
        "facts" -> o.facts,
        "resources" -> o.resources,
        "rules" -> o.rules,
        "enable_ttl" -> o.enableTtl,
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
          enableTtl = (json \ "enable_ttl").asOpt[Boolean].getOrElse(false),
          ttl = (json \ "ttl").asOpt[Long].map(_.millis).getOrElse(1.hour),
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }
}

case class BiscuitTokenForge(
  id: String,
  name: String,
  description: String = "",
  keypairRef: String = "",
  config: BiscuitForgeConfig,
  tags: Seq[String] = Seq.empty,
  metadata: Map[String, String] = Map.empty,
  location: EntityLocation,
  remoteFactsLoaderRef: Option[String] = None
) extends EntityLocationSupport {
  def json: JsValue = BiscuitTokenForge.format.writes(this)

  def internalId: String = id

  def theDescription: String = description

  def theMetadata: Map[String, String] = metadata

  def theName: String = name

  def theTags: Seq[String] = tags

  def forgeToken(ctx: JsValue)(implicit env: Env, ec: ExecutionContext): Future[Either[String, Biscuit]] = {
    env.adminExtensions.extension[BiscuitExtension].get.states.keypair(keypairRef) match {
      case None => Left("keypair not found").vfuture
      case Some(kp) => {
        remoteFactsLoaderRef match {
          case None => {
            createToken(kp.privKey) match {
              case Left(err) => Left("unable to forge token").vfuture
              case Right(token) => Right(token).vfuture
            }
          }
          case Some(remoteFactsRef) => {
            env.adminExtensions.extension[BiscuitExtension].get.states.biscuitRemoteFactsLoader(remoteFactsRef) match {
              case None => Left("remote facts reference not found").vfuture
              case Some(remoteFacts) => {
                remoteFacts.loadFacts(ctx).flatMap {
                  case Left(error) => Left(error).vfuture
                  case Right(remoteFacts) => {

                    val finalConfig = config.copy(
                      facts = remoteFacts.facts ++ remoteFacts.acl ++ remoteFacts.roles,
                    )

                    finalConfig.createToken(kp.privKey) match {
                      case Left(err) => Left("unable to forge token").vfuture
                      case Right(token) => Right(token).vfuture
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  def createToken(privKeyValue: String)(implicit env: Env): Either[String, Biscuit] = {
    config.createToken(privKeyValue)
  }
}

object BiscuitTokenForge {
  val format = new Format[BiscuitTokenForge] {
    override def writes(o: BiscuitTokenForge): JsValue = {
      Json.obj(
        "id" -> o.id,
        "name" -> o.name,
        "description" -> o.description,
        "metadata" -> o.metadata,
        "keypair_ref" -> o.keypairRef,
        "config" -> o.config.json,
        "tags" -> JsArray(o.tags.map(JsString.apply)),
        "remote_facts_ref" -> o.remoteFactsLoaderRef
      )
    }

    override def reads(json: JsValue): JsResult[BiscuitTokenForge] =
      Try {
        BiscuitTokenForge(
          location = EntityLocation.readFromKey(json),
          id = (json \ "id").as[String],
          name = (json \ "name").as[String],
          description = (json \ "description").asOpt[String].getOrElse("--"),
          keypairRef = (json \ "keypair_ref").asOpt[String].getOrElse("--"),
          metadata = (json \ "metadata").asOpt[Map[String, String]].getOrElse(Map.empty),
          tags = (json \ "tags").asOpt[Seq[String]].getOrElse(Seq.empty[String]),
          config = json.select("config").asOpt(BiscuitForgeConfig.format).getOrElse(BiscuitForgeConfig()),
          remoteFactsLoaderRef = json.select("remote_facts_ref").asOpt[String]
        )
      } match {
        case Failure(e) => JsError(e.getMessage)
        case Success(e) => JsSuccess(e)
      }
  }

  def resource(env: Env, datastores: BiscuitExtensionDatastores, states: BiscuitExtensionState): Resource = {
    Resource(
      "BiscuitForge",
      "biscuit-forges",
      "biscuit-forge",
      "biscuit.extensions.cloud-apim.com",
      ResourceVersion("v1", true, false, true),
      GenericResourceAccessApiWithState[BiscuitTokenForge](
        format = BiscuitTokenForge.format,
        clazz = classOf[BiscuitTokenForge],
        keyf = id => datastores.biscuitTokenForgeDataStore.key(id),
        extractIdf = c => datastores.biscuitTokenForgeDataStore.extractId(c),
        extractIdJsonf = json => json.select("id").asString,
        idFieldNamef = () => "id",
        tmpl = (v, p) => {
          BiscuitTokenForge(
            id = IdGenerator.namedId("biscuit-forge", env),
            name = "New biscuit forge",
            description = "New biscuit forge",
            location = EntityLocation.default,
            config = BiscuitForgeConfig()
          ).json
        },
        canRead = true,
        canCreate = true,
        canUpdate = true,
        canDelete = true,
        canBulk = true,
        stateAll = () => states.allBiscuitTokenForge(),
        stateOne = id => states.biscuitTokenForge(id),
        stateUpdate = values => states.updateBiscuitTokenForge(values)
      )
    )
  }
}

trait BiscuitTokenForgeDataStore extends BasicStore[BiscuitTokenForge]

class KvBiscuitTokenForgeDataStore(extensionId: AdminExtensionId, redisCli: RedisLike, _env: Env)
  extends BiscuitTokenForgeDataStore
    with RedisLikeStore[BiscuitTokenForge] {
  override def fmt: Format[BiscuitTokenForge] = BiscuitTokenForge.format

  override def redisLike(implicit env: Env): RedisLike = redisCli

  override def key(id: String): String = s"${_env.storageRoot}:extensions:${extensionId.cleanup}:biscuit:forge:$id"

  override def extractId(value: BiscuitTokenForge): String = value.id
}