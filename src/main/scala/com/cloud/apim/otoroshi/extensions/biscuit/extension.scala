package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit

import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import biscuit.format.schema.Schema.PublicKey.Algorithm
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import com.cloud.apim.otoroshi.extensions.biscuit.utils.{BiscuitForgeConfig, BiscuitRemoteUtils, BiscuitUtils}
import org.biscuitsec.biscuit.crypto.{KeyPair, PublicKey}
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.env.Env
import otoroshi.models._
import otoroshi.next.extensions._
import otoroshi.utils.cache.types.UnboundedTrieMap
import otoroshi.utils.syntax.implicits._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{RequestHeader, Result, Results}

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
      path = "/extensions/cloud-apim/extensions/biscuit/tokens/_generate",
      wantsBody = true,
      handle = handleGenerateToken
    ),
    AdminExtensionBackofficeAuthRoute(
      method = "POST",
      path = "/extensions/cloud-apim/extensions/biscuit/tokens/verifier/_test",
      wantsBody = true,
      handle = handleVerifierTester
    ),
    AdminExtensionBackofficeAuthRoute(
      method = "POST",
      path = "/extensions/cloud-apim/extensions/biscuit/remote-facts/_test",
      wantsBody = true,
      handle = handleTestRemoteFacts
    )
  )

  def handleGenerateToken(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body: Option[Source[ByteString, _]]): Future[Result] = {
    generateTokenFromBody(body, false)
  }

  def generateTokenFromBody(body: Option[Source[ByteString, _]], isAdminApiRoute: Boolean): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer

    (body match {
      case None => handleError("no body", isAdminApiRoute)
      case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
        val bodyJson = bodyRaw.utf8String.parseJson

        val keypairPubKey = bodyJson.select("pubKey").asOpt[String]
        val keypairPrivKey = bodyJson.select("privKey").asOpt[String]

        if (keypairPubKey.isDefined && keypairPrivKey.isDefined) {
          createTokenWithConfig(bodyJson, keypairPrivKey.get, isAdminApiRoute)
        } else {
          bodyJson.select("keypair_ref").asOpt[String] match {
            case None => handleError("no keypair or keypair_ref provided", isAdminApiRoute)
            case Some(keyPairRef) => {
              env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keyPairRef)) match {
                case None => handleError("no keypair found", isAdminApiRoute)
                case Some(keypairDb) => {
                  createTokenWithConfig(bodyJson, keypairDb.privKey, isAdminApiRoute)
                }
              }
            }
          }
        }
      }
    }).recover {
      case e: Throwable => {
        if (isAdminApiRoute) {
          Results.InternalServerError(Json.obj("error" -> e.getMessage))
        } else {
          Results.Ok(Json.obj("done" -> false, "error" -> e.getMessage))
        }
      }
    }
  }

  private def createTokenWithConfig(bodyJson: JsValue, privKey: String, adminApiRoute: Boolean): Future[Result] = {
    bodyJson.select("config").asOpt[JsValue] match {
      case None => handleError("no config provided", adminApiRoute)
      case Some(newTokenConfig) => {

        val biscuitForgeConf = BiscuitForgeConfig.format.reads(newTokenConfig).asOpt

        biscuitForgeConf match {
          case None => handleError("unable to parse biscuit forge configuration", adminApiRoute)
          case Some(biscuitForgeConfig) => {
            val generatedToken = BiscuitUtils.createToken(privKey, biscuitForgeConfig)


            if (adminApiRoute) {
              Results.Ok(
                Json.obj(
                  "token" -> generatedToken.serialize_b64url()
                )
              ).vfuture
            } else {
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

  def handleError(errorMessage: String, isAdminApiRoute: Boolean): Future[Result] = {
    if (isAdminApiRoute) {
      Results.BadRequest(Json.obj("error" -> errorMessage)).vfuture
    } else {
      Results.Ok(Json.obj("done" -> false, "error" -> errorMessage)).vfuture
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
          val biscuitForgeRef = bodyJson.select("biscuitForgeRef").asOpt[String]
          val biscuitToken = bodyJson.select("biscuitToken").asOpt[String]
          val biscuitKeyPairRef = bodyJson.select("keypairRef").asOpt[String]
          val verifierConfigBody = bodyJson.select("config").asOpt[JsValue]

          if (verifierConfigBody.isDefined) {
            val verifierConfig = VerifierConfig.format.reads(verifierConfigBody.get).asOpt

            verifierConfig match {
              case None => Results.Ok(Json.obj("done" -> false, "error" -> "keypairRef not provided")).vfuture
              case Some(config) => {

                biscuitKeyPairRef match {
                  case None => Results.Ok(Json.obj("done" -> false, "error" -> "keypairRef not provided")).vfuture
                  case Some(keypairRef) => {
                    if (biscuitToken.isDefined && biscuitToken.nonEmpty && biscuitToken.get.trim.nonEmpty) {
                      verifyWithTokenInput(keypairRef, biscuitToken.get, config)
                    } else {
                      if (biscuitForgeRef.isDefined && biscuitForgeRef.nonEmpty) {
                        env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitTokenForge(biscuitForgeRef.get)) match {
                          case None => Results.Ok(Json.obj("done" -> false, "error" -> "biscuitTokenRef doesn't exist")).vfuture
                          case Some(biscuitForge) => {

                            biscuitForge.config match {
                              case None => Results.Ok(Json.obj("done" -> false, "error" -> "bad config from token forge")).vfuture
                              case Some(forgeConfig) => {
                                verifyWithForgeInput(keypairRef, forgeConfig, config)
                              }
                            }
                          }
                        }

                      } else {
                        Results.Ok(Json.obj("done" -> false, "error" -> "biscuit forge ref or biscuit token not found in request body")).vfuture
                      }
                    }
                  }
                }

              }
            }
          } else {
            Results.Ok(Json.obj("done" -> false, "error" -> "Verifier config not provided")).vfuture
          }

        }
    }).recover {
      case e: Throwable =>
        Results.Ok(Json.obj("done" -> false, "error" -> e.getMessage))
    }
  }

  private def verifyWithTokenInput(keypairRef: String, inputToken: String, verifierConfig: VerifierConfig): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env

    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keypairRef)) match {
      case None => Results.Ok(Json.obj("done" -> false, "error" -> "keypair doesn't exist")).vfuture
      case Some(keypair) => {
        val publicKey = new PublicKey(biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519, keypair.pubKey)

        Try(Biscuit.from_b64url(inputToken, publicKey)).toEither match {
          case Left(err) => {
            Results.Ok(Json.obj("done" -> false, "error" -> s"Unable to deserialize Biscuit token : ${err}")).vfuture
          }
          case Right(biscuitUnverified) =>

            Try(biscuitUnverified.verify(publicKey)).toEither match {
              case Left(err) => Results.Ok(Json.obj("done" -> false, "error" -> s"Biscuit token is not valid : ${err}")).vfuture
              case Right(biscuitToken) => {

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

  private def verifyWithForgeInput(keypairRef: String, forgeConfigInput: BiscuitForgeConfig, verifierConfig: VerifierConfig): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env

    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keypairRef)) match {
      case None => Results.Ok(Json.obj("done" -> false, "error" -> "keypair doesn't exist")).vfuture
      case Some(keypair) => {
        val publicKey = new PublicKey(biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519, keypair.pubKey)

        val generatedToken = BiscuitUtils.createToken(keypair.privKey, forgeConfigInput).serialize_b64url()

        Try(Biscuit.from_b64url(generatedToken, publicKey)).toEither match {
          case Left(err) => {
            Results.Ok(Json.obj("done" -> false, "error" -> s"Unable to deserialize Biscuit token : ${err}")).vfuture
          }
          case Right(biscuitUnverified) =>

            Try(biscuitUnverified.verify(publicKey)).toEither match {
              case Left(err) => Results.Ok(Json.obj("done" -> false, "error" -> s"Biscuit token is not valid : ${err}")).vfuture
              case Right(biscuitToken) => {

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

  def handleTestRemoteFacts(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body: Option[Source[ByteString, _]]): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env

    (body match {
      case None => Results.Ok(Json.obj("done" -> false, "error" -> "no body")).vfuture
      case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
        val bodyJson = bodyRaw.utf8String.parseJson

        val headersOpt = bodyJson.select("headers").asOpt[Map[String, String]]
        val apiUrlOpt = bodyJson.select("apiUrl").asOpt[String]

        apiUrlOpt match {
          case None => Results.Ok(Json.obj("done" -> false, "error" -> "no api URL provided")).vfuture
          case Some(apiURL) => {

            BiscuitRemoteUtils.getRemoteFacts(apiURL, headersOpt.getOrElse(Map.empty)).flatMap {
              case Left(error) => Results.Ok(Json.obj("done" -> false, "error" -> s"unable to load remote facts - ${error}")).vfuture
              case Right(listFacts) => {
                Results.Ok(
                  Json.obj(
                    "done" -> true,
                    "loadedFacts" -> Json.obj(
                      "rolesRemotes" -> listFacts._1,
                      "revokedIds" -> listFacts._2,
                      "facts" -> listFacts._3,
                      "acl" -> listFacts._4
                    )
                  )
                ).vfuture
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
        Results.Ok(treeSitterComponent).as("application/wasm").vfuture
      }
    ),
    AdminExtensionAssetRoute(
      path = "/extensions/assets/cloud-apim/extensions/biscuit/assets/tree-sitter-biscuit.wasm",
      handle = (ctx: AdminExtensionRouterContext[AdminExtensionAssetRoute], req: RequestHeader) => {
        Results.Ok(treeSitterBiscuitComponent).as("application/wasm").vfuture
      }
    ),
    AdminExtensionAssetRoute(
      path = "/extensions/assets/cloud-apim/extensions/biscuit/assets/biscuit.wasm",
      handle = (ctx: AdminExtensionRouterContext[AdminExtensionAssetRoute], req: RequestHeader) => {
        Results.Ok(biscuitWasmComponents).as("application/wasm").vfuture
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
             |          link: '/extensions/cloud-apim/biscuit/token-forges',
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
             |          link: '/extensions/cloud-apim/biscuit/token-forges',
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
             |          path: 'extensions/cloud-apim/biscuit/token-forges',
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
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/token-forges`
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
             |          path: '/extensions/cloud-apim/biscuit/token-forges/:taction/:titem',
             |          component: (props) => {
             |            return React.createElement(BiscuitTokenForge, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/token-forges/:taction',
             |          component: (props) => {
             |            return React.createElement(BiscuitTokenForge, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/token-forges',
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

  override def adminApiRoutes(): Seq[AdminExtensionAdminApiRoute] = Seq(
    AdminExtensionAdminApiRoute(
      "POST",
      "/api/extensions/biscuit/tokens/_generate",
      wantsBody = true,
      (ctx, request, apk, body) => {
        generateTokenFromBody(body, isAdminApiRoute = true)
      }
    ),
    AdminExtensionAdminApiRoute(
      "POST",
      "/api/extensions/biscuit/token-forges/:id/_generate",
      wantsBody = true,
      (ctx, request, apk, body) => {
        ctx.named("id") match {
          case None => Results.NotFound(Json.obj("error" -> "Path parameter id is not found")).vfuture
          case Some(forgeId) => {
            env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitTokenForge(forgeId)) match {
              case None => Results.NotFound(Json.obj("error" -> "Forge not found")).vfuture
              case Some(forge) => {
                forge.config match {
                  case None => handleError("no config found", isAdminApiRoute = true)
                  case Some(finalConfig) => {
                    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(forge.keypairRef)) match {
                      case None => handleError("no keypair found", isAdminApiRoute = true)
                      case Some(keypairDb) => {
                        val generatedToken = BiscuitUtils.createToken(keypairDb.privKey, finalConfig)
                        Results.Ok(
                          Json.obj(
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
    )
  )
}