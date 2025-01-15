package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit

import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import akka.util.ByteString
import akka.stream.scaladsl.{Source, StreamConverters}
import biscuit.format.schema.Schema.PublicKey.Algorithm
import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitForgeConfig, BiscuitUtils}
import org.biscuitsec.biscuit.crypto.{KeyPair, PublicKey}
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.env.Env
import otoroshi.models._
import otoroshi.next.extensions._
import otoroshi.utils.cache.types.UnboundedTrieMap
import otoroshi.utils.syntax.implicits._
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{RequestHeader, Result, Results}
import otoroshi.plugins.biscuit.VerificationContext

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Try

class BiscuitExtensionDatastores(env: Env, extensionId: AdminExtensionId) {
  val biscuitKeyPairDataStore: BiscuitKeyPairDataStore = new KvBiscuitKeyPairDataStore(extensionId, env.datastores.redis, env)
  val biscuitVerifierDataStore: BiscuitVerifierDataStore = new KvBiscuitVerifierDataStore(extensionId, env.datastores.redis, env)
  val biscuitAttenuatorDataStore: BiscuitAttenuatorDataStore = new KvBiscuitAttenuatorDataStore(extensionId, env.datastores.redis, env)
  val biscuitTokenForgeDataStore: BiscuitTokenForgeDataStore = new KvBiscuitTokenForgeDataStore(extensionId, env.datastores.redis, env)
  val biscuitRbacPolicyDataStore: BiscuitRbacPolicyDataStore = new KvBiscuitRbacPolicyDataStore(extensionId, env.datastores.redis, env)
  val biscuitRemoteFactsLoaderDataStore: BiscuitRemoteFactsLoaderDataStore = new KvBiscuitRemoteFactsLoaderDataStore(extensionId, env.datastores.redis, env)
}

class BiscuitExtensionState(env: Env) {

  private val _keypairs = new UnboundedTrieMap[String, BiscuitKeyPair]()
  private val _verifiers = new UnboundedTrieMap[String, BiscuitVerifier]()
  private val _attenuators = new UnboundedTrieMap[String, BiscuitAttenuator]()
  private val _tokenforge = new UnboundedTrieMap[String, BiscuitTokenForge]()
  private val _rbacpolicies = new UnboundedTrieMap[String, BiscuitRbacPolicy]()
  private val _rfl = new UnboundedTrieMap[String, RemoteFactsLoader]()

  def keypair(id: String): Option[BiscuitKeyPair] = _keypairs.get(id)

  def allKeypairs(): Seq[BiscuitKeyPair] = _keypairs.values.toSeq

  def updateKeyPairs(values: Seq[BiscuitKeyPair]): Unit = {
    _keypairs.addAll(values.map(v => (v.id, v))).remAll(_keypairs.keySet.toSeq.diff(values.map(_.id)))
  }

  def biscuitVerifier(id: String): Option[BiscuitVerifier] = _verifiers.get(id)

  def allBiscuitVerifiers(): Seq[BiscuitVerifier] = _verifiers.values.toSeq

  def updateBiscuitVerifiers(values: Seq[BiscuitVerifier]): Unit = {
    _verifiers.addAll(values.map(v => (v.id, v))).remAll(_verifiers.keySet.toSeq.diff(values.map(_.id)))
  }

  def biscuitAttenuator(id: String): Option[BiscuitAttenuator] = _attenuators.get(id)

  def allBiscuitAttenuators(): Seq[BiscuitAttenuator] = _attenuators.values.toSeq

  def updateBiscuitAttenuators(values: Seq[BiscuitAttenuator]): Unit = {
    _attenuators.addAll(values.map(v => (v.id, v))).remAll(_attenuators.keySet.toSeq.diff(values.map(_.id)))
  }

  def biscuitTokenForge(id: String): Option[BiscuitTokenForge] = _tokenforge.get(id)

  def allBiscuitTokenForge(): Seq[BiscuitTokenForge] = _tokenforge.values.toSeq

  def updateBiscuitTokenForge(values: Seq[BiscuitTokenForge]): Unit = {
    _tokenforge.addAll(values.map(v => (v.id, v))).remAll(_tokenforge.keySet.toSeq.diff(values.map(_.id)))
  }

  def biscuitRbacPolicy(id: String): Option[BiscuitRbacPolicy] = _rbacpolicies.get(id)

  def allbiscuitRbacPolicies(): Seq[BiscuitRbacPolicy] = _rbacpolicies.values.toSeq

  def updateBiscuitRbacPolicy(values: Seq[BiscuitRbacPolicy]): Unit = {
    _rbacpolicies.addAll(values.map(v => (v.id, v))).remAll(_rbacpolicies.keySet.toSeq.diff(values.map(_.id)))
  }

  def biscuitRemoteFactsLoader(id: String): Option[RemoteFactsLoader] = _rfl.get(id)

  def allBiscuitRemoteFactsLoader(): Seq[RemoteFactsLoader] = _rfl.values.toSeq

  def updatebiscuitRemoteFactsLoader(values: Seq[RemoteFactsLoader]): Unit = {
    _rfl.addAll(values.map(v => (v.id, v))).remAll(_rfl.keySet.toSeq.diff(values.map(_.id)))
  }
}

class BiscuitExtension(val env: Env) extends AdminExtension {

  lazy val states = new BiscuitExtensionState(env)
  lazy val biscuitKeyPairPage = getResourceCode("cloudapim/extensions/biscuit/BiscuitKeyPairPage.js")
  lazy val biscuitVerifiersPage = getResourceCode("cloudapim/extensions/biscuit/BiscuitVerifiersPage.js")
  lazy val biscuitAttenuatorsPage = getResourceCode("cloudapim/extensions/biscuit/BiscuitAttenuatorPage.js")
  lazy val biscuitTokenForgePage = getResourceCode("cloudapim/extensions/biscuit/BiscuitTokenForgePage.js")
  lazy val biscuitRbacPoliciesPage = getResourceCode("cloudapim/extensions/biscuit/BiscuitRbacPoliciesPage.js")
  lazy val biscuitRemoteFactsLoaderPage = getResourceCode("cloudapim/extensions/biscuit/BiscuitRemoteFactsLoaderPage.js")
  lazy val biscuitWebComponents = getResourceCode("cloudapim/extensions/biscuit/webcomponents/index.js")
    .replace("/assets/tree-sitter.wasm", "/extensions/assets/cloud-apim/extensions/biscuit/assets/tree-sitter.wasm")
    .replace("/assets/tree-sitter-biscuit.wasm", "/extensions/assets/cloud-apim/extensions/biscuit/assets/tree-sitter-biscuit.wasm")
    .replace("assets/biscuit_bg-f81a6772.wasm", "/extensions/assets/cloud-apim/extensions/biscuit/assets/biscuit.wasm")
  lazy val treeSitterComponent = getResourceBytes("cloudapim/extensions/biscuit/webcomponents/assets/tree-sitter.wasm")
  lazy val treeSitterBiscuitComponent = getResourceBytes("cloudapim/extensions/biscuit/webcomponents/assets/tree-sitter-biscuit.wasm")
  lazy val biscuitWasmComponents = getResourceBytes("cloudapim/extensions/biscuit/webcomponents/assets/biscuit.wasm")
  private lazy val datastores = new BiscuitExtensionDatastores(env, id)
  val logger = Logger("cloud-apim-biscuit-extension")

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

  def getResourceCode(path: String): String = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    env.environment.resourceAsStream(path)
      .map(stream => StreamConverters.fromInputStream(() => stream).runFold(ByteString.empty)(_ ++ _).awaitf(10.seconds).utf8String)
      .getOrElse(s"'resource ${path} not found !'")
  }

  def getResourceBytes(path: String): ByteString = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    env.environment.resourceAsStream(path)
      .map(stream => StreamConverters.fromInputStream(() => stream).runFold(ByteString.empty)(_ ++ _).awaitf(10.seconds))
      .get
  }

  override def backofficeAuthRoutes(): Seq[AdminExtensionBackofficeAuthRoute] = Seq(
    AdminExtensionBackofficeAuthRoute(
      method = "POST",
      path = "/extensions/cloud-apim/extensions/biscuit/tokens/forge/_generate",
      wantsBody = true,
      handle = handleGenerateTokenFromForge
    ),
    AdminExtensionBackofficeAuthRoute(
      method = "POST",
      path = "/extensions/cloud-apim/extensions/biscuit/tokens/verifier/_test",
      wantsBody = true,
      handle = handleVerifierTester
    )
  )

  def handleGenerateTokenFromForge(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body: Option[Source[ByteString, _]]): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    (body match {
      case None => Results.Ok(Json.obj("done" -> false, "error" -> "no body")).vfuture
      case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
        val bodyJson = bodyRaw.utf8String.parseJson
        bodyJson.select("keypair_ref").asOpt[String] match {
          case None => Results.Ok(Json.obj("done" -> false, "error" -> "no keypair provided")).vfuture
          case Some(keyPairRef) => {
            env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keyPairRef)) match {
              case None => Results.Ok(Json.obj("done" -> false, "error" -> "no keypair found")).vfuture
              case Some(keypairDb) => {
                bodyJson.select("config").asOpt[JsValue] match {
                  case None => Results.Ok(Json.obj("done" -> false, "error" -> "no config provided")).vfuture
                  case Some(newTokenConfig) => {

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

  def handleVerifierTester(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body: Option[Source[ByteString, _]]): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env
    (body match {
      case None => Results.Ok(Json.obj("done" -> false, "error" -> "no body")).vfuture
      case Some(bodySource) =>
        bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
          val bodyJson = bodyRaw.utf8String.parseJson
          val biscuitTokenRef = bodyJson.select("biscuitTokenRef").asOpt[String]
          val biscuitKeyPairRef = bodyJson.select("keypairRef").asOpt[String]

          biscuitKeyPairRef match {
            case None => Results.Ok(Json.obj("done" -> false, "error" -> "keypairRef not provided")).vfuture
            case Some(keypairRef) => {

              env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitTokenForge(biscuitTokenRef.get)) match {
                case None => Results.Ok(Json.obj("done" -> false, "error" -> "biscuitTokenRef doesn't exist")).vfuture
                case Some(biscuitTokenRef) => {

                  if (biscuitTokenRef.token.isEmpty) {
                    Results.Ok(Json.obj("done" -> false, "error" -> "biscuit token not provided in entity")).vfuture
                  } else {
                    val finalBiscuitToken = biscuitTokenRef.token.get

                    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keypairRef)) match {
                      case None => Results.Ok(Json.obj("done" -> false, "error" -> "keypair doesn't exist")).vfuture
                      case Some(keypair) => {
                        val publicKey = new PublicKey(biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519, keypair.pubKey)


                        Try(Biscuit.from_b64url(finalBiscuitToken, publicKey)).toEither match {
                          case Left(err) => {
                            Results.Ok(Json.obj("done" -> false, "error" -> s"Unable to deserialize Biscuit token : ${err}")).vfuture
                          }
                          case Right(biscuitUnverified) =>

                            Try(biscuitUnverified.verify(publicKey)).toEither match {
                              case Left(err) => Results.Ok(Json.obj("done" -> false, "error" -> s"Biscuit token is not valid : ${err}")).vfuture
                              case Right(biscuitToken) => {

                                val verifierConfig = VerifierConfig(
                                  checks = bodyJson.select("checks").asOpt[List[String]].getOrElse(List.empty),
                                  facts = bodyJson.select("facts").asOpt[List[String]].getOrElse(List.empty),
                                  resources = bodyJson.select("resources").asOpt[List[String]].getOrElse(List.empty),
                                  rules = bodyJson.select("rules").asOpt[List[String]].getOrElse(List.empty),
                                  policies = bodyJson.select("policies").asOpt[List[String]].getOrElse(List.empty),
                                  revokedIds = bodyJson.select("revokedIds").asOpt[List[String]].getOrElse(List.empty),
                                )

                                BiscuitUtils.verify(biscuitToken, verifierConfig, None) match {
                                  case Left(err) => Results.Ok(Json.obj("done" -> false, "error" -> err.toString)).vfuture
                                  case Right(_) => Results.Ok(Json.obj("done" -> true, "message" -> "Checked successfully")).vfuture
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
          }
        }
    }).recover {
      case e: Throwable =>
        Results.Ok(Json.obj("done" -> false, "error" -> e.getMessage))
    }
  }

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
      path = "/extensions/assets/cloud-apim/extensions/biscuit/assets/tree-sitter.wasm",
      handle = (ctx: AdminExtensionRouterContext[AdminExtensionAssetRoute], req: RequestHeader) => {
        Results.Ok(treeSitterComponent).future
      }
    ),
    AdminExtensionAssetRoute(
      path = "/extensions/assets/cloud-apim/extensions/biscuit/assets/tree-sitter-biscuit.wasm",
      handle = (ctx: AdminExtensionRouterContext[AdminExtensionAssetRoute], req: RequestHeader) => {
        Results.Ok(treeSitterBiscuitComponent).future
      }
    ),
    AdminExtensionAssetRoute(
      path = "/extensions/assets/cloud-apim/extensions/biscuit/assets/biscuit.wasm",
      handle = (ctx: AdminExtensionRouterContext[AdminExtensionAssetRoute], req: RequestHeader) => {
        Results.Ok(biscuitWasmComponents).future
      }
    ),
    AdminExtensionAssetRoute(
      path = "/extensions/assets/cloud-apim/extensions/biscuit/biscuit.js",
      handle = (ctx: AdminExtensionRouterContext[AdminExtensionAssetRoute], req: RequestHeader) => {
        Results.Ok(biscuitWebComponents).as("text/javascript").vfuture
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
             |    ${biscuitRbacPoliciesPage}
             |    ${biscuitRemoteFactsLoaderPage}
             |
             |    const s = document.createElement("script")
             |    s.setAttribute("type", "module")
             |    s.setAttribute("src", "/extensions/assets/cloud-apim/extensions/biscuit/biscuit.js")
             |
             |    document.body.appendChild(s)
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
             |         {
             |          title: 'Biscuit RBAC Policies',
             |          description: 'All your Biscuit RBAC Policies',
             |          link: '/extensions/cloud-apim/biscuit/rbac',
             |          display: () => true,
             |          icon: () => 'fa-list-check'
             |        },
             |        {
             |          title: 'Biscuit Remote Facts Loader',
             |          description: 'All your Biscuit Remote Facts Loader',
             |          link: '/extensions/cloud-apim/biscuit/remote-facts',
             |          display: () => true,
             |          icon: () => 'fa-tower-broadcast'
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
             |         {
             |          title: 'Biscuit RBAC Policies',
             |          description: 'All your Biscuit RBAC Policies',
             |          link: '/extensions/cloud-apim/biscuit/rbac',
             |          display: () => true,
             |          icon: () => 'fa-list-check'
             |        },
             |         {
             |          title: 'Biscuit Remote Facts Loader',
             |          description: 'All your Biscuit Remote Facts Loader',
             |          link: '/extensions/cloud-apim/biscuit/remote-facts',
             |          display: () => true,
             |          icon: () => 'fa-tower-broadcast'
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
             |         {
             |          title: 'Biscuit RBAC Policies',
             |          text: 'All your Biscuit RBAC Policies',
             |          path: 'extensions/cloud-apim/biscuit/rbac',
             |          icon: 'list-check'
             |        },
             |         {
             |          title: 'Biscuit Remote Facts Loader',
             |          text: 'All your Biscuit Remote Facts Loader',
             |          link: '/extensions/cloud-apim/biscuit/remote-facts',
             |          display: () => true,
             |          icon: () => 'tower-broadcast'
             |        },
             |      ],
             |      searchItems: [
             |        {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/keypairs`
             |          },
             |          env: React.createElement('span', { className: "fas fa-key" }, null),
             |          label: 'Biscuit KeyPairs',
             |          value: 'biscuitkeypairs',
             |        },
             |         {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/verifiers`
             |          },
             |          env: React.createElement('span', { className: "fas fa-circle-check" }, null),
             |          label: 'Biscuit Verifiers',
             |          value: 'biscuitverifiers',
             |        },
             |        {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/attenuators`
             |          },
             |          env: React.createElement('span', { className: "fas fa-volume-low" }, null),
             |          label: 'Biscuit Attenuators',
             |          value: 'biscuitattenuators',
             |        },
             |         {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/tokens-forge`
             |          },
             |          env: React.createElement('span', { className: "fas fa-hammer" }, null),
             |          label: 'Biscuit Tokens Forge',
             |          value: 'tokens-forge',
             |        },
             |          {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/rbac`
             |          },
             |          env: React.createElement('span', { className: "fas fa-list-check" }, null),
             |          label: 'Biscuit RBAC Policies',
             |          value: 'biscuit-rbac',
             |        },
             |         {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/remote-facts`
             |          },
             |          env: React.createElement('span', { className: "fas fa-tower-broadcast" }, null),
             |          label: 'Biscuit Remote Facts Loader',
             |          value: 'biscuit-facts-loader',
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
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/rbac/:taction/:titem',
             |          component: (props) => {
             |            return React.createElement(BiscuitRbacPoliciesPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/rbac/:taction',
             |          component: (props) => {
             |            return React.createElement(BiscuitRbacPoliciesPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/rbac',
             |          component: (props) => {
             |            return React.createElement(BiscuitRbacPoliciesPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/remote-facts/:taction/:titem',
             |          component: (props) => {
             |            return React.createElement(BiscuitRemoteFactsLoaderPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/remote-facts/:taction',
             |          component: (props) => {
             |            return React.createElement(BiscuitRemoteFactsLoaderPage, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/remote-facts',
             |          component: (props) => {
             |            return React.createElement(BiscuitRemoteFactsLoaderPage, props, null)
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

  override def id: AdminExtensionId = AdminExtensionId("cloud-apim.extensions.biscuit")

  override def syncStates(): Future[Unit] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val ev = env
    for {
      keypairs <- datastores.biscuitKeyPairDataStore.findAllAndFillSecrets()
      verifiers <- datastores.biscuitVerifierDataStore.findAllAndFillSecrets()
      attenuators <- datastores.biscuitAttenuatorDataStore.findAllAndFillSecrets()
      tokenForge <- datastores.biscuitTokenForgeDataStore.findAllAndFillSecrets()
      rbacPolicies <- datastores.biscuitRbacPolicyDataStore.findAllAndFillSecrets()
      remoteFactsLoader <- datastores.biscuitRemoteFactsLoaderDataStore.findAllAndFillSecrets()
    } yield {
      states.updateKeyPairs(keypairs)
      states.updateBiscuitVerifiers(verifiers)
      states.updateBiscuitAttenuators(attenuators)
      states.updateBiscuitTokenForge(tokenForge)
      states.updateBiscuitRbacPolicy(rbacPolicies)
      states.updatebiscuitRemoteFactsLoader(remoteFactsLoader)
      ()
    }
  }

  override def entities(): Seq[AdminExtensionEntity[EntityLocationSupport]] = {
    Seq(
      AdminExtensionEntity(BiscuitKeyPair.resource(env, datastores, states)),
      AdminExtensionEntity(BiscuitVerifier.resource(env, datastores, states)),
      AdminExtensionEntity(BiscuitAttenuator.resource(env, datastores, states)),
      AdminExtensionEntity(BiscuitTokenForge.resource(env, datastores, states)),
      AdminExtensionEntity(BiscuitRbacPolicy.resource(env, datastores, states)),
      AdminExtensionEntity(RemoteFactsLoader.resource(env, datastores, states)),
    )
  }
}