package otoroshi.jobs.revokedbicsuit

import com.cloud.apim.otoroshi.extensions.biscuit.entities.BiscuitRemoteFactsConfig
import otoroshi.env.Env
import otoroshi.next.plugins.api.NgPluginCategory
import otoroshi.script.*
import otoroshi.utils.syntax.implicits.*
import otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.biscuitExtension
import play.api.Logger
import play.api.libs.json.Json

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}

// TODO: add some kind of way to query a remote system, config from the config file (job that add list of ids every 5 minutes ?)
class RevokedTokensJob extends Job {

  private val logger = Logger("otoroshi-biscuit-studio-revoked-tokens-job")

  override def categories: Seq[NgPluginCategory] = Seq.empty

  override def uniqueId: JobId = JobId("io.otoroshi.core.jobs.RevokedTokensJob")

  override def name: String = "Otoroshi Biscuit Studio - Revoked tokens job"

  override def jobVisibility: JobVisibility = JobVisibility.Internal

  override def kind: JobKind = JobKind.ScheduledEvery

  override def initialDelay(ctx: JobContext, env: Env): Option[FiniteDuration] =
    env.biscuitExtension.configuration.getOptional[Int]("revocation_job.revocation_initial_delay_time").map(_.seconds.some).getOrElse(10.seconds.some)

  override def interval(ctx: JobContext, env: Env): Option[FiniteDuration] =
    env.biscuitExtension.configuration.getOptional[Int]("revocation_job.revocation_interval").map(_.seconds.some).getOrElse(60.seconds.some)

  override def starting: JobStarting = JobStarting.Automatically

  override def instantiation(ctx: JobContext, env: Env): JobInstantiation =
    JobInstantiation.OneInstancePerOtoroshiCluster

  override def predicate(ctx: JobContext, env: Env): Option[Boolean] = None

  override def jobRun(ctx: JobContext)(using env: Env, ec: ExecutionContext): Future[Unit] = {
    val isJobActive = env.biscuitExtension.configuration.getOptional[Boolean]("revocation_job.enabled").getOrElse(false)

    val apiUrl = env.biscuitExtension.configuration.getOptional[String]("revocation_job.api_url")
    val apiMethod = env.biscuitExtension.configuration.getOptional[String]("revocation_job.api_method")
    val apiHeaders = env.biscuitExtension.configuration.getOptional[Map[String, String]]("revocation_job.api_headers").getOrElse(Map.empty)

    if (isJobActive && apiUrl.isDefined && apiUrl.isDefined) {
      logger.info("loading new revoked tokens from remote facts ...")

      val rfconfig = BiscuitRemoteFactsConfig(
        apiUrl = apiUrl.get,
        method = apiMethod.get,
        headers = apiHeaders
      )

      rfconfig.getRemoteFacts(Json.obj()).flatMap {
        case Left(_) => ().vfuture
        case Right(rfdata) => {
          if (rfdata.revoked.nonEmpty) {
            rfdata.revoked.map {
              token =>
                env.biscuitExtension.datastores.biscuitRevocationDataStore.add(
                  id = token,
                  reason = "Job".some
                )
            }
          }
          ().vfuture
        }
      }
    } else {
      ().vfuture
    }
  }
}