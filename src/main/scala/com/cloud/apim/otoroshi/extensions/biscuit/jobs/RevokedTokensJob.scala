package otoroshi.jobs.revokedbicsuit

import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitRemoteFactsConfig
import otoroshi.env.Env
import otoroshi.next.plugins.api.NgPluginCategory
import otoroshi.script.{Job, JobContext, JobId, JobInstantiation, JobKind, JobStarting, JobVisibility}
import play.api.Logger
import otoroshi.utils.syntax.implicits._
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.BiscuitExtension
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

// TODO: add some kind of way to query a remote system, config from the config file (job that add list of ids every 5 minutes ?)
class RevokedTokensJob extends Job {

  private val logger = Logger("otoroshi-biscuit-studio-revoked-tokens-job")

  override def categories: Seq[NgPluginCategory] = Seq.empty

  override def uniqueId: JobId = JobId("io.otoroshi.core.jobs.RevokedTokensJob")

  override def name: String = "Otoroshi Biscuit Studio - Revoked tokens job"

  override def jobVisibility: JobVisibility = JobVisibility.Internal

  override def kind: JobKind = JobKind.ScheduledEvery

  override def initialDelay(ctx: JobContext, env: Env): Option[FiniteDuration] = 20.seconds.some

  override def interval(ctx: JobContext, env: Env): Option[FiniteDuration] = 1.minutes.some

  override def starting: JobStarting = JobStarting.Automatically

  override def instantiation(ctx: JobContext, env: Env): JobInstantiation =
    JobInstantiation.OneInstancePerOtoroshiCluster

  override def predicate(ctx: JobContext, env: Env): Option[Boolean] = None

  override def jobRun(ctx: JobContext)(implicit env: Env, ec: ExecutionContext): Future[Unit] = {
    logger.info("loading new revoked tokens from remote facts ...")

    //TODO: load from remote system new revoked tokens

    //TODO: from config
    val rfconfig = BiscuitRemoteFactsConfig(
      apiUrl = "http://localhost:3333/api/revoked",
      method = "GET"
    )

    rfconfig.getRemoteFacts(Json.obj()).flatMap{
      case Left(_) => ().vfuture
      case Right(rfdata) => {
        if(rfdata.revoked.nonEmpty){
          rfdata.revoked.map{
            token =>
              env.adminExtensions.extension[BiscuitExtension].get.datastores.biscuitRevocationDataStore.add(
                id = token,
                reason = "Job".some
              )
          }
        }
        ().vfuture
      }
    }
  }
}