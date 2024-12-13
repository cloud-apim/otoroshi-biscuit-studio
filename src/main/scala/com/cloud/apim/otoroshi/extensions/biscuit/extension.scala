package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit

import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import akka.util.ByteString
import akka.stream.scaladsl.{Source, StreamConverters}
import biscuit.format.schema.Schema.PublicKey.Algorithm
import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitForgeConfig, BiscuitUtils}
import org.biscuitsec.biscuit.crypto.{KeyPair, PublicKey}
import otoroshi.env.Env
import otoroshi.models._
import otoroshi.next.extensions._
import otoroshi.utils.cache.types.UnboundedTrieMap
import otoroshi.utils.syntax.implicits._
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class BiscuitExtensionDatastores(env: Env, extensionId: AdminExtensionId) {
  val biscuitKeyPairDataStore: BiscuitKeyPairDataStore = new KvBiscuitKeyPairDataStore(extensionId, env.datastores.redis, env)
  val biscuitVerifierDataStore: BiscuitVerifierDataStore = new KvBiscuitVerifierDataStore(extensionId, env.datastores.redis, env)
  val biscuitAttenuatorDataStore: BiscuitAttenuatorDataStore = new KvBiscuitAttenuatorDataStore(extensionId, env.datastores.redis, env)
  val biscuitTokenForgeDataStore: BiscuitTokenForgeDataStore = new KvBiscuitTokenForgeDataStore(extensionId, env.datastores.redis, env)
}

class BiscuitExtensionState(env: Env) {

  private val _keypairs = new UnboundedTrieMap[String, BiscuitKeyPair]()
  def keypair(id: String): Option[BiscuitKeyPair] = _keypairs.get(id)
  def allKeypairs(): Seq[BiscuitKeyPair]          = _keypairs.values.toSeq
  def updateKeyPairs(values: Seq[BiscuitKeyPair]): Unit = {
    _keypairs.addAll(values.map(v => (v.id, v))).remAll(_keypairs.keySet.toSeq.diff(values.map(_.id)))
  }

  private val _verifiers = new UnboundedTrieMap[String, BiscuitVerifier]()
  def biscuitVerifier(id: String): Option[BiscuitVerifier] = _verifiers.get(id)
  def allBiscuitVerifiers(): Seq[BiscuitVerifier] = _verifiers.values.toSeq
  def updateBiscuitVerifiers(values: Seq[BiscuitVerifier]): Unit = {
    _verifiers.addAll(values.map(v => (v.id, v))).remAll(_verifiers.keySet.toSeq.diff(values.map(_.id)))
  }

  private val _attenuators = new UnboundedTrieMap[String, BiscuitAttenuator]()
  def biscuitAttenuator(id: String): Option[BiscuitAttenuator] = _attenuators.get(id)
  def allBiscuitAttenuators(): Seq[BiscuitAttenuator] = _attenuators.values.toSeq
  def updateBiscuitAttenuators(values: Seq[BiscuitAttenuator]): Unit = {
    _attenuators.addAll(values.map(v => (v.id, v))).remAll(_attenuators.keySet.toSeq.diff(values.map(_.id)))
  }

  private val _tokenforge = new UnboundedTrieMap[String, BiscuitTokenForge]()
  def biscuitTokenForge(id: String): Option[BiscuitTokenForge] = _tokenforge.get(id)
  def allBiscuitTokenForge(): Seq[BiscuitTokenForge] = _tokenforge.values.toSeq
  def updateBiscuitTokenForge(values: Seq[BiscuitTokenForge]): Unit = {
    _tokenforge.addAll(values.map(v => (v.id, v))).remAll(_tokenforge.keySet.toSeq.diff(values.map(_.id)))
  }
}

class BiscuitExtension(val env: Env) extends AdminExtension {

  private lazy val datastores = new BiscuitExtensionDatastores(env, id)

  lazy val states = new BiscuitExtensionState(env)

  val logger = Logger("cloud-apim-biscuit-extension")

  override def id: AdminExtensionId = AdminExtensionId("cloud-apim.extensions.biscuit")

  override def name: String = "Otoroshi Biscuit Studio"

  override def description: Option[String] = "This extensions provides Biscuit Tokens implementation to your Otoroshi instances".some

  override def enabled: Boolean = env.isDev || configuration.getOptional[Boolean]("enabled").getOrElse(false)

  override def start(): Unit = {
    logger.info("the 'Biscuit Extension' is enabled !")
    implicit val ev = env
    implicit val ec = env.otoroshiExecutionContext
  }

  override def stop(): Unit = {
  }

  override def frontendExtensions(): Seq[AdminExtensionFrontendExtension] = Seq(
    AdminExtensionFrontendExtension(
      path = "/extensions/assets/cloud-apim/extensions/biscuit/extension.js"
    )
  )

  lazy val biscuitKeyPairPage = getResourceCode("cloudapim/extensions/biscuit/BiscuitKeyPairPage.js")
  lazy val biscuitVerifiersPage = getResourceCode("cloudapim/extensions/biscuit/BiscuitVerifiersPage.js")
  lazy val biscuitAttenuatorsPage = getResourceCode("cloudapim/extensions/biscuit/BiscuitAttenuatorPage.js")
  lazy val biscuitTokenForgePage = getResourceCode("cloudapim/extensions/biscuit/BiscuitTokenForgePage.js")

  def handleGenerateTokenFromForge(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body:  Option[Source[ByteString, _]]): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    (body match {
      case None => Results.Ok(Json.obj("done" -> false, "error" -> "no body")).vfuture
      case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
        val bodyJson = bodyRaw.utf8String.parseJson
        bodyJson.select("keypair_ref").asOpt[String] match {
          case None => Results.Ok(Json.obj("done" -> false, "error" -> "no keypair provided")).vfuture
          case Some(keyPairRef) =>  {
            env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keyPairRef)) match {
              case None => Results.Ok(Json.obj("done" -> false, "error" -> "no keypair found")).vfuture
              case Some(keypairDb) => {
                bodyJson.select("config").asOpt[JsValue] match {
                  case None => Results.Ok(Json.obj("done" -> false, "error" -> "no config provided")).vfuture
                  case Some(newTokenConfig) =>  {

                    val biscuitForgeConf = BiscuitForgeConfig.format.reads(newTokenConfig).asOpt

                    biscuitForgeConf match {
                      case None => Results.Ok(Json.obj("done" -> false, "error" -> "unable to parse biscuit forge configuration")).vfuture
                      case Some(biscuitForgeConfig) => {

                        val generatedToken = BiscuitUtils.createToken(keypairDb.privKey, biscuitForgeConfig)

                        Results.Ok(
                          Json.obj(
                            "done" -> true,
                            "token" -> generatedToken.serialize_b64url()
                          )
                        ).vfuture
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }).recover {
      case e: Throwable => {
        Results.Ok(Json.obj("done" -> false, "error" -> e.getMessage))
      }
    }
  }

  def getResourceCode(path: String): String = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    env.environment.resourceAsStream(path)
      .map(stream => StreamConverters.fromInputStream(() => stream).runFold(ByteString.empty)(_++_).awaitf(10.seconds).utf8String)
      .getOrElse(s"'resource ${path} not found !'")
  }

  override def backofficeAuthRoutes(): Seq[AdminExtensionBackofficeAuthRoute] = Seq(
    AdminExtensionBackofficeAuthRoute(
      method = "POST",
      path = "/extensions/cloud-apim/extensions/biscuit/tokens/forge/_generate",
      wantsBody = true,
      handle = handleGenerateTokenFromForge
    )
  )

  override def assets(): Seq[AdminExtensionAssetRoute] = Seq(
    AdminExtensionAssetRoute(
      path = "/extensions/assets/cloud-apim/extensions/biscuit/keypairs/generate",
      handle = (ctx: AdminExtensionRouterContext[AdminExtensionAssetRoute], req: RequestHeader) => {
        val generatedKeyPair = KeyPair.generate(Algorithm.Ed25519)

        Results.Ok(
          Json.obj(
            "publickey" -> generatedKeyPair.public_key().toHex.toLowerCase,
            "privateKey" -> generatedKeyPair.toHex.toLowerCase
          )
        ).as("application/json").vfuture
      }
    ),
    AdminExtensionAssetRoute(
      path = "/extensions/assets/cloud-apim/extensions/biscuit/extension.js",
      handle = (ctx: AdminExtensionRouterContext[AdminExtensionAssetRoute], req: RequestHeader) => {
        Results.Ok(
          s"""(function() {
             |  const extensionId = "${id.value}";
             |  Otoroshi.registerExtension(extensionId, false, (ctx) => {
             |
             |    const dependencies = ctx.dependencies;
             |
             |    const React     = dependencies.react;
             |    const _         = dependencies.lodash;
             |    const Component = React.Component;
             |    const uuid      = dependencies.uuid;
             |    const Table     = dependencies.Components.Inputs.Table;
             |    const Form      = dependencies.Components.Inputs.Form;
             |    const SelectInput = dependencies.Components.Inputs.SelectInput;
             |    const BackOfficeServices = dependencies.BackOfficeServices;
             |
             |    ${biscuitKeyPairPage}
             |    ${biscuitVerifiersPage}
             |    ${biscuitAttenuatorsPage}
             |    ${biscuitTokenForgePage}
             |
             |    return {
             |      id: extensionId,
             |      categories:[{
             |        title: 'Biscuit Studio',
             |        description: 'All the features provided the Cloud APIM Biscuit Studio extension',
             |        features: [
             |          {
             |          title: 'Biscuit KeyPairs',
             |          description: 'All your Biscuit KeyPairs',
             |          link: '/extensions/cloud-apim/biscuit/keypairs',
             |          display: () => true,
             |          icon: () => 'fa-key',
             |        },
             |         {
             |          title: 'Biscuit Verifiers',
             |          description: 'All your Biscuit Verifiers',
             |          link: '/extensions/cloud-apim/biscuit/verifiers',
             |          display: () => true,
             |          icon: () => 'fa-circle-check',
             |        },
             |        {
             |          title: 'Biscuit Attenuators',
             |          description: 'All your Biscuit Attenuators',
             |          link: '/extensions/cloud-apim/biscuit/attenuators',
             |          display: () => true,
             |          icon: () => 'fa-volume-low',
             |        },
             |        {
             |          title: 'Tokens Forge',
             |          description: 'All your Biscuit Tokens',
             |          link: '/extensions/cloud-apim/biscuit/tokens-forge',
             |          display: () => true,
             |          icon: () => 'fa-hammer',
             |        },
             |        ]
             |      }],
             |      features: [
             |        {
             |          title: 'Biscuit KeyPairs',
             |          description: 'All your Biscuit KeyPairs',
             |          link: '/extensions/cloud-apim/biscuit/keypairs',
             |          display: () => true,
             |          icon: () => 'fa-key',
             |        },
             |         {
             |          title: 'Biscuit Verifiers',
             |          description: 'All your Biscuit Verifiers',
             |          link: '/extensions/cloud-apim/biscuit/verifiers',
             |          display: () => true,
             |          icon: () => 'fa-circle-check',
             |        },
             |        {
             |          title: 'Biscuit Attenuators',
             |          description: 'All your Biscuit Attenuators',
             |          link: '/extensions/cloud-apim/biscuit/attenuators',
             |          display: () => true,
             |          icon: () => 'fa-volume-low',
             |        },
             |        {
             |          title: 'Tokens Forge',
             |          description: 'All your Biscuit Tokens',
             |          link: '/extensions/cloud-apim/biscuit/tokens-forge',
             |          display: () => true,
             |          icon: () => 'fa-hammer',
             |        },
             |      ],
             |      sidebarItems: [
             |        {
             |          title: 'Biscuit KeyPairs',
             |          text: 'All your Biscuit KeyPairs',
             |          path: 'extensions/cloud-apim/biscuit/keypairs',
             |          icon: 'key'
             |        },
             |         {
             |          title: 'Biscuit Verifiers',
             |          text: 'All your Biscuit Verifiers',
             |          path: 'extensions/cloud-apim/biscuit/verifiers',
             |          icon: 'circle-check'
             |        },
             |        {
             |          title: 'Biscuit Attenuators',
             |          text: 'All your Biscuit Attenuators',
             |          path: 'extensions/cloud-apim/biscuit/attenuators',
             |          icon: 'volume-low'
             |        },
             |         {
             |          title: 'Tokens Forge',
             |          text: 'All your Biscuit Tokens',
             |          path: 'extensions/cloud-apim/biscuit/tokens-forge',
             |          icon: 'hammer'
             |        },
             |      ],
             |      searchItems: [
             |        {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/keypairs`
             |          },
             |          env: React.createElement('span', { className: "fas fa-brain" }, null),
             |          label: 'Biscuit KeyPairs',
             |          value: 'biscuitkeypairs',
             |        },
             |         {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/verifiers`
             |          },
             |          env: React.createElement('span', { className: "fas fa-brain" }, null),
             |          label: 'Biscuit Verifiers',
             |          value: 'biscuitverifiers',
             |        },
             |        {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/attenuators`
             |          },
             |          env: React.createElement('span', { className: "fas fa-brain" }, null),
             |          label: 'Biscuit Attenuators',
             |          value: 'biscuitattenuators',
             |        },
             |         {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/tokens-forge`
             |          },
             |          env: React.createElement('span', { className: "fas fa-brain" }, null),
             |          label: 'Biscuit Tokens Forge',
             |          value: 'tokens-forge',
             |        },
             |      ],
             |      routes: [
             |        {
             |          path: '/extensions/cloud-apim/biscuit/keypairs/:taction/:titem',
             |          component: (props) => {
             |            return React.createElement(BiscuitKeyPairPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/keypairs/:taction',
             |          component: (props) => {
             |            return React.createElement(BiscuitKeyPairPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/keypairs',
             |          component: (props) => {
             |            return React.createElement(BiscuitKeyPairPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/verifiers/:taction/:titem',
             |          component: (props) => {
             |            return React.createElement(BiscuitVerifiersPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/verifiers/:taction',
             |          component: (props) => {
             |            return React.createElement(BiscuitVerifiersPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/verifiers',
             |          component: (props) => {
             |            return React.createElement(BiscuitVerifiersPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/tokens-forge/:taction/:titem',
             |          component: (props) => {
             |            return React.createElement(BiscuitTokenForge, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/tokens-forge/:taction',
             |          component: (props) => {
             |            return React.createElement(BiscuitTokenForge, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/tokens-forge',
             |          component: (props) => {
             |            return React.createElement(BiscuitTokenForge, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/attenuators/:taction/:titem',
             |          component: (props) => {
             |            return React.createElement(BiscuitAttenuatorPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/attenuators/:taction',
             |          component: (props) => {
             |            return React.createElement(BiscuitAttenuatorPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/attenuators',
             |          component: (props) => {
             |            return React.createElement(BiscuitAttenuatorPage, props, null)
             |          }
             |        }
             |      ]
             |    }
             |  });
             |})();
             |""".stripMargin).as("application/javascript").vfuture
      }
    )
  )

  override def syncStates(): Future[Unit] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val ev = env
    for {
      keypairs <- datastores.biscuitKeyPairDataStore.findAllAndFillSecrets()
      verifiers <- datastores.biscuitVerifierDataStore.findAllAndFillSecrets()
      attenuators <- datastores.biscuitAttenuatorDataStore.findAllAndFillSecrets()
      tokenForge <- datastores.biscuitTokenForgeDataStore.findAllAndFillSecrets()
    } yield {
      states.updateKeyPairs(keypairs)
      states.updateBiscuitVerifiers(verifiers)
      states.updateBiscuitAttenuators(attenuators)
      states.updateBiscuitTokenForge(tokenForge)
      ()
    }
  }

  override def entities(): Seq[AdminExtensionEntity[EntityLocationSupport]] = {
    Seq(
      AdminExtensionEntity(BiscuitKeyPair.resource(env, datastores, states)),
      AdminExtensionEntity(BiscuitVerifier.resource(env, datastores, states)),
      AdminExtensionEntity(BiscuitAttenuator.resource(env, datastores, states)),
      AdminExtensionEntity(BiscuitTokenForge.resource(env, datastores, states)),
    )
  }
}