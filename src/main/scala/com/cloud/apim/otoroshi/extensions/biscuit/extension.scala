package otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit

import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import com.cloud.apim.otoroshi.extensions.biscuit.entities._
import com.cloud.apim.otoroshi.extensions.biscuit.utils.BiscuitUtils
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator
import org.biscuitsec.biscuit.crypto.{KeyPair, PublicKey}
import org.biscuitsec.biscuit.token.Biscuit
import otoroshi.env.Env
import otoroshi.models._
import otoroshi.next.extensions._
import otoroshi.next.utils.JsonHelpers
import otoroshi.security.IdGenerator
import otoroshi.utils.TypedMap
import otoroshi.utils.cache.types.UnboundedTrieMap
import otoroshi.utils.syntax.implicits._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BiscuitExtensionDatastores(env: Env, extensionId: AdminExtensionId) {
  val biscuitKeyPairDataStore: BiscuitKeyPairDataStore = new KvBiscuitKeyPairDataStore(extensionId, env.datastores.redis, env)
  val biscuitVerifierDataStore: BiscuitVerifierDataStore = new KvBiscuitVerifierDataStore(extensionId, env.datastores.redis, env)
  val biscuitAttenuatorDataStore: BiscuitAttenuatorDataStore = new KvBiscuitAttenuatorDataStore(extensionId, env.datastores.redis, env)
  val biscuitTokenForgeDataStore: BiscuitTokenForgeDataStore = new KvBiscuitTokenForgeDataStore(extensionId, env.datastores.redis, env)
  val biscuitRbacPolicyDataStore: BiscuitRbacPolicyDataStore = new KvBiscuitRbacPolicyDataStore(extensionId, env.datastores.redis, env)
  val biscuitRemoteFactsLoaderDataStore: BiscuitRemoteFactsLoaderDataStore = new KvBiscuitRemoteFactsLoaderDataStore(extensionId, env.datastores.redis, env)
  val biscuitRevocationDataStore: RevocationDatastore = new RevocationDatastore()(env)
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

  def allPublicKeyPairs(authorizedKeys: Seq[String]): Seq[BiscuitKeyPair] = {
    if (authorizedKeys.nonEmpty)
      _keypairs.values.toSeq.filter(kp => authorizedKeys.contains(kp.id))
    else _keypairs.values.toSeq.filter(kp => kp.isPublic)
  }

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
  lazy val biscuitRevocation = getResourceCode("cloudapim/extensions/biscuit/BiscuitRevocation.js")
  lazy val biscuitWebComponents = getResourceCode("cloudapim/extensions/biscuit/webcomponents/index.js")
    .replace("/assets/tree-sitter.wasm", "/extensions/assets/cloud-apim/extensions/biscuit/assets/tree-sitter.wasm")
    .replace("/assets/tree-sitter-biscuit.wasm", "/extensions/assets/cloud-apim/extensions/biscuit/assets/tree-sitter-biscuit.wasm")
    .replace("assets/biscuit_bg-f81a6772.wasm", "/extensions/assets/cloud-apim/extensions/biscuit/assets/biscuit.wasm")
  lazy val treeSitterComponent = getResourceBytes("cloudapim/extensions/biscuit/webcomponents/assets/tree-sitter.wasm")
  lazy val treeSitterBiscuitComponent = getResourceBytes("cloudapim/extensions/biscuit/webcomponents/assets/tree-sitter-biscuit.wasm")
  lazy val biscuitWasmComponents = getResourceBytes("cloudapim/extensions/biscuit/webcomponents/assets/biscuit.wasm")
  lazy val datastores = new BiscuitExtensionDatastores(env, id)
  val logger = Logger("cloud-apim-biscuit-extension")

  override def name: String = "Otoroshi Biscuit Studio"

  override def description: Option[String] = "This extensions provides Biscuit Tokens implementation to your Otoroshi instances".some

  override def enabled: Boolean = env.isDev || configuration.getOptional[Boolean]("enabled").getOrElse(false)

  override def start(): Unit = {
    logger.info("the 'Biscuit Extension' is enabled !")
    WorkflowFunctionsInitializer.initDefaults()
  }

  override def stop(): Unit = {
  }

  override def frontendExtensions(): Seq[AdminExtensionFrontendExtension] = Seq(
    AdminExtensionFrontendExtension(
      path = "/extensions/assets/cloud-apim/extensions/biscuit/extension.js"
    )
  )

  override def publicKeys(): Future[Seq[PublicKeyJwk]] = {
    env.adminExtensions.extension[BiscuitExtension].get.states.allKeypairs().map {
      keypair => {

        val algoCurve = keypair.algo.toUpperCase match {
          case "ED25519" => Curve.Ed25519
//          case "SECP256K1" => Curve.SECP256K1 -- waiting for release of new keypair algorithms
          case _ => Curve.Ed25519
        }

        val jwk = new OctetKeyPairGenerator(algoCurve).keyID(keypair.id).generate()
        val publicJWK = jwk.toPublicJWK.toJSONString.parseJson
        PublicKeyJwk(publicJWK)
      }
    }.vfuture
  }

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
      path = "/extensions/cloud-apim/extensions/biscuit/keypairs/_generate",
      wantsBody = true,
      handle = handleGenerateKeypair
    ),
    AdminExtensionBackofficeAuthRoute(
      method = "POST",
      path = "/extensions/cloud-apim/extensions/biscuit/tokens/_generate",
      wantsBody = true,
      handle = handleGenerateToken
    ),
    AdminExtensionBackofficeAuthRoute(
      method = "POST",
      path = "/extensions/cloud-apim/extensions/biscuit/tokens/verifiers/_test",
      wantsBody = true,
      handle = handleVerifierTester
    ),
    AdminExtensionBackofficeAuthRoute(
      method = "POST",
      path = "/extensions/cloud-apim/extensions/biscuit/remote-facts/_test",
      wantsBody = true,
      handle = handleTestRemoteFacts
    ),
    AdminExtensionBackofficeAuthRoute(
      method = "POST",
      path = "/extensions/cloud-apim/extensions/biscuit/tokens/attenuators/_test",
      wantsBody = true,
      handle = handleAttenuatorTester
    ),
    // Routes for tokens revocation
    AdminExtensionBackofficeAuthRoute(
      method = "GET",
      path = "/extensions/cloud-apim/extensions/biscuit/tokens/revocation/_all",
      wantsBody = false,
      handle = handleGetAllRevokedTokens
    ),
    AdminExtensionBackofficeAuthRoute(
      method = "POST",
      path = "/extensions/cloud-apim/extensions/biscuit/tokens/revocation/_revoke",
      wantsBody = true,
      handle = handleRevokeToken
    )
  )

  def handleGetAllRevokedTokens(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body: Option[Source[ByteString, _]]): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env
    env.adminExtensions.extension[BiscuitExtension].get.datastores.biscuitRevocationDataStore.list().map {
      tokens =>
        Results.Ok(
          Json.obj(
            "tokens" -> tokens.map(_.json)
          )
        )
    }
  }

  def handleRevokeToken(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body: Option[Source[ByteString, _]]): Future[Result] = {

    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env

    (body match {
      case None => Results.Ok(Json.obj("done" -> false, "error" -> "no body")).vfuture
      case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
        val bodyJson = bodyRaw.utf8String.parseJson

        RevokedToken.format.reads(bodyJson) match {
          case JsSuccess(token, _) => {
            if (token.revocationId.nonEmpty) {
              datastores.biscuitRevocationDataStore.add(
                token.revocationId,
                token.reason.some
              )
            }
          }
          case _ => ()
        }

        Results.Ok(
          Json.obj(
            "done" -> true,
          )
        ).vfuture

      }
    })
  }

  def handleGenerateKeypair(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body: Option[Source[ByteString, _]]): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env

    (body match {
      case None => Results.Ok(Json.obj("done" -> false, "error" -> "no body")).vfuture
      case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
        val bodyJson = bodyRaw.utf8String.parseJson

        val algoInput = bodyJson.select("algorithm").asOpt[String].getOrElse("ED25519")

        val algo = algoInput.toUpperCase match {
          case "ED25519" => "Ed25519"
          // case "SECP256R1" => "SECP256R1"
          case _ => "Ed25519"
        }

        val pkAlgo = algo.toUpperCase match {
          case "ED25519" => biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519
          //      case "SECP256R1" => biscuit.format.schema.Schema.PublicKey.Algorithm.SECP256R1
          case _ => biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519
        }

        val generatedKeyPair = KeyPair.generate(pkAlgo)

        val pubKey = generatedKeyPair.public_key().toHex.toUpperCase
        val privKey = generatedKeyPair.toHex.toUpperCase

        Results.Ok(
          Json.obj(
            "done" -> true,
            "algorithm" -> algo,
            "pubKey" -> pubKey,
            "privKey" -> privKey,
            "algoPubKey" -> generatedKeyPair.public_key().toString
          )
        ).vfuture
      }
    }).recover {
      case e: Throwable => {
        Results.Ok(Json.obj("done" -> false, "error" -> e.getMessage))
      }
    }
  }

  def handleGenerateToken(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body: Option[Source[ByteString, _]]): Future[Result] = {
    generateTokenFromBody(body, isAdminApiRoute = false)
  }

  def generateTokenFromBody(body: Option[Source[ByteString, _]], isAdminApiRoute: Boolean): Future[Result] = {
    implicit val ev = env
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer

    (body match {
      case None => handleError("no body", isAdminApiRoute)
      case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
        val bodyJson = bodyRaw.utf8String.parseJson

        val keypairPubKeyOpt = bodyJson.select("pubKey").asOpt[String]
        val keypairPrivKeyOpt = bodyJson.select("privKey").asOpt[String]
        val keypairRefOpt = bodyJson.select("keypair_ref").asOpt[String]

        keypairRefOpt match {
          case Some(keyPairRef) => {
            env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keyPairRef)) match {
              case Some(keypairDb) => createTokenWithKpRefAndConfig(bodyJson, keypairDb.id, isAdminApiRoute)
              case None => handleError("no keypair entity found", isAdminApiRoute)
            }
          }
          case None => {
            (keypairPubKeyOpt, keypairPrivKeyOpt) match {
              case (Some(pubKey), Some(privKey)) => {
                createTokenWithPubPrivKeysAndConfig(bodyJson, pubKey, privKey, isAdminApiRoute)
              }
              case _ => handleError("no keypair or keypair_ref provided", isAdminApiRoute)
            }
          }
        }
      }
    }).recover {
      case e: Throwable => {
        if (isAdminApiRoute) {
          Results.InternalServerError(
            Json.obj(
              "error" -> e.getMessage
            )
          )
        } else {
          Results.Ok(
            Json.obj(
              "done" -> false,
              "error" -> e.getMessage
            )
          )
        }
      }
    }
  }

  private def createTokenWithKpRefAndConfig(bodyJson: JsValue, keypairId: String, isAdminApiRoute: Boolean)(implicit env: Env, ec: ExecutionContext): Future[Result] = {
    bodyJson.select("config").asOpt[JsValue] match {
      case None => handleError("no config provided", isAdminApiRoute)
      case Some(newTokenConfig) => {

        val biscuitForgeConf = BiscuitForgeConfig.format.reads(newTokenConfig).asOpt.getOrElse(BiscuitForgeConfig())
        val remoteFactsRef = bodyJson.select("remoteFactsLoaderRef").asOpt[String]

        env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keypairId)) match {
          case None => handleError("keypair entity not found", isAdminApiRoute)
          case Some(keypair) => {

            val forgeConfig = BiscuitTokenForge(
              id = IdGenerator.namedId("biscuit-forge", env),
              name = "New Biscuit Forge",
              description = "New biscuit Forge",
              config = biscuitForgeConf,
              location = EntityLocation.default,
              remoteFactsLoaderRef = remoteFactsRef,
              keypairRef = keypair.id
            )

            forgeConfig.forgeToken().flatMap {
              case Left(err) => handleError(s"Can't create the biscuit token ${err}", isAdminApiRoute)
              case Right(token) => {
                if (isAdminApiRoute) {
                  Results.Ok(
                    Json.obj(
                      "pubKey" -> keypair.pubKey,
                      "token" -> token.serialize_b64url()
                    )
                  ).vfuture
                } else {
                  Results.Ok(
                    Json.obj(
                      "done" -> true,
                      "pubKey" -> keypair.pubKey,
                      "token" -> token.serialize_b64url()
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

  private def createTokenWithPubPrivKeysAndConfig(bodyJson: JsValue, pubKey: String, privKey: String, isAdminApiRoute: Boolean)(implicit env: Env, ec: ExecutionContext): Future[Result] = {
    if (pubKey.isEmpty || privKey.isEmpty) {
      handleError("public or private key not provided", isAdminApiRoute)
    } else {

      bodyJson.select("config").asOpt[JsValue] match {
        case None => handleError("no config provided", isAdminApiRoute)
        case Some(newTokenConfig) => {

          val biscuitForgeConf = BiscuitForgeConfig.format.reads(newTokenConfig).asOpt.getOrElse(BiscuitForgeConfig())
          val remoteFactsLoaderRef = bodyJson.select("remoteFactsLoaderRef").asOpt[String]


          remoteFactsLoaderRef match {
            case None => {
              biscuitForgeConf.createToken(privKey) match {
                case Left(err) => handleError(s"unable to forge the token with config : ${err}", isAdminApiRoute)
                case Right(token) => {
                  if (isAdminApiRoute) {
                    Results.Ok(
                      Json.obj(
                        "pubKey" -> pubKey,
                        "token" -> token.serialize_b64url()
                      )
                    ).vfuture
                  } else {
                    Results.Ok(
                      Json.obj(
                        "done" -> true,
                        "pubKey" -> pubKey,
                        "token" -> token.serialize_b64url()
                      )
                    ).vfuture
                  }
                }
              }
            }
            case Some(remoteFactsRef) => {
              env.adminExtensions.extension[BiscuitExtension].get.states.biscuitRemoteFactsLoader(remoteFactsRef) match {
                case None => handleError(s"remote facts entity not found", isAdminApiRoute)
                case Some(remoteFacts) => {
                  remoteFacts.loadFacts().flatMap {
                    case Left(err) => handleError(s"unable to forge the token with config : ${err}", isAdminApiRoute)
                    case Right(remoteFacts) => {

                      val finalConfig = biscuitForgeConf.copy(
                        facts = biscuitForgeConf.facts ++ remoteFacts.facts ++ remoteFacts.acl ++ remoteFacts.roles,
                      )

                      finalConfig.createToken(privKey) match {
                        case Left(err) => handleError(s"Can't create the biscuit token ${err}", isAdminApiRoute)
                        case Right(token) => {
                          if (isAdminApiRoute) {
                            Results.Ok(
                              Json.obj(
                                "pubKey" -> pubKey,
                                "token" -> token.serialize_b64url()
                              )
                            ).vfuture
                          } else {
                            Results.Ok(
                              Json.obj(
                                "done" -> true,
                                "pubKey" -> pubKey,
                                "token" -> token.serialize_b64url()
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
        }
      }
    }
  }

  def handleVerifierTester(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body: Option[Source[ByteString, _]]): Future[Result] = {
    verifyTokenFromBody(body, isAdminApiRoute = false)
  }

  def verifyTokenFromBody(body: Option[Source[ByteString, _]], isAdminApiRoute: Boolean): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env
    (body match {
      case None => handleError("no body provided", isAdminApiRoute)
      case Some(bodySource) =>
        bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
          val bodyJson = bodyRaw.utf8String.parseJson
          val biscuitForgeRef = bodyJson.select("forge_ref").asOpt[String]
          val biscuitToken = bodyJson.select("token").asOpt[String]
          val biscuitKeyPairRef = bodyJson.select("keypair_ref").asOpt[String]
          val verifierConfigBody = bodyJson.select("config").asOpt[JsValue]

          if (verifierConfigBody.isDefined) {
            val verifierConfig = VerifierConfig.format.reads(verifierConfigBody.get).asOpt

            verifierConfig match {
              case None => handleError("verifier config not provided or bad formatted", isAdminApiRoute)
              case Some(config) => {

                biscuitKeyPairRef match {
                  case None => handleError("keypairRef is empty", isAdminApiRoute)
                  case Some(keypairRef) => {
                    if (biscuitToken.isDefined && biscuitToken.nonEmpty && biscuitToken.get.trim.nonEmpty) {
                      verifyWithTokenInput(keypairRef, biscuitToken.get, config, isAdminApiRoute)
                    } else {
                      if (biscuitForgeRef.isDefined && biscuitForgeRef.nonEmpty) {
                        env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitTokenForge(biscuitForgeRef.get)) match {
                          case None => handleError("forge is not provided", isAdminApiRoute)
                          case Some(biscuitForge) => {

                            verifyWithForgeInput(keypairRef, biscuitForge, config, isAdminApiRoute)
                          }
                        }

                      } else {
                        handleError("biscuit forge ref or biscuit token not found in request body", isAdminApiRoute)
                      }
                    }
                  }
                }

              }
            }
          } else {
            handleError("Verifier config not provided", isAdminApiRoute)
          }

        }
    }).recover {
      case e: Throwable =>
        if (isAdminApiRoute) {
          Results.InternalServerError(Json.obj("error" -> e.getMessage))
        } else {
          Results.Ok(Json.obj("done" -> false, "error" -> e.getMessage))
        }
    }
  }

  private def verifyWithTokenInput(keypairRef: String, inputToken: String, verifierConfig: VerifierConfig, isAdminApiRoute: Boolean): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env

    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keypairRef)) match {
      case None => handleError("keypair entity not found", isAdminApiRoute)
      case Some(keypair) => {
        val publicKey = new PublicKey(keypair.getCurrentAlgo, keypair.pubKey)

        Try(Biscuit.from_b64url(inputToken, publicKey)).toEither match {
          case Left(err: org.biscuitsec.biscuit.error.Error) => handleError(BiscuitUtils.handleBiscuitErrors(err), isAdminApiRoute)
          case Left(err) => handleError(s"Unable to deserialize Biscuit token : ${err.getMessage}", isAdminApiRoute)

          case Right(biscuitToken) => {
            verifierConfig.verify(biscuitToken, None) flatMap {
              case Left(err) => handleError(err, isAdminApiRoute)
              case Right(_) => {
                if (isAdminApiRoute) {
                  Results.Ok(
                    Json.obj(
                      "status" -> "success",
                      "message" -> "Checked successfully"
                    )
                  ).vfuture
                } else {
                  Results.Ok(Json.obj(
                    "status" -> "success",
                    "done" -> true,
                    "message" -> "Checked successfully"
                  )).vfuture
                }
              }
            }
          }
        }
      }
    }
  }

  private def verifyWithForgeInput(verifierKeyPairRef: String, forge: BiscuitTokenForge, verifierConfig: VerifierConfig, isAdminApiRoute: Boolean): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env

    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(verifierKeyPairRef)) match {
      case None => handleError("keypair entity not found", isAdminApiRoute)
      case Some(keypair) => {
        val verifierPublicKey = new PublicKey(keypair.getCurrentAlgo, keypair.pubKey)

        forge.forgeToken().flatMap {
          case Left(err) => Results.Ok(Json.obj("done" -> false, "error" -> err)).vfuture
          case Right(biscuitToken) => {
            val generatedToken = biscuitToken.serialize_b64url()

            Try(Biscuit.from_b64url(generatedToken, verifierPublicKey)).toEither match {
              case Left(err: org.biscuitsec.biscuit.error.Error) => handleError(BiscuitUtils.handleBiscuitErrors(err), isAdminApiRoute)
              case Left(err) => handleError(s"Unable to deserialize Biscuit token : ${err.getMessage}", isAdminApiRoute)
              case Right(biscuitToken) => {
                verifierConfig.verify(biscuitToken, None) flatMap {
                  case Left(err) => handleError(err, isAdminApiRoute)
                  case Right(_) => {
                    if (isAdminApiRoute) {
                      Results.Ok(Json.obj(
                        "status" -> "success",
                        "message" -> "Checked successfully"
                      )).vfuture
                    } else {
                      Results.Ok(Json.obj(
                        "status" -> "success",
                        "done" -> true,
                        "message" -> "Checked successfully"
                      )).vfuture
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

  def handleError(errorMessage: String, isAdminApiRoute: Boolean): Future[Result] = {
    if (isAdminApiRoute) {
      Results.BadRequest(
        Json.obj(
          "status" -> "error",
          "error" -> errorMessage
        )
      ).vfuture
    } else {
      Results.Ok(
        Json.obj(
          "status" -> "error",
          "done" -> false,
          "error" -> errorMessage
        )
      ).vfuture
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
        BiscuitRemoteFactsConfig.format.reads(bodyJson) match {
          case JsError(errors) => Results.Ok(Json.obj("done" -> false, "error" -> "no api URL provided")).vfuture
          case JsSuccess(config, path) => {
            val remoteCtx = bodyJson.select("remote_ctx").asOpt[JsObject].getOrElse(Json.obj()) ++ Json.obj("phase" -> "test_remote_facts_loader")
            config.getRemoteFacts(remoteCtx).flatMap {
              case Left(error) => Results.Ok(Json.obj("done" -> false, "error" -> s"unable to load remote facts - ${error}")).vfuture
              case Right(listFacts) => {
                Results.Ok(
                  Json.obj(
                    "done" -> true,
                    "loadedFacts" -> listFacts.json
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

  def handleAttenuatorTester(ctx: AdminExtensionRouterContext[AdminExtensionBackofficeAuthRoute], req: RequestHeader, user: Option[BackOfficeUser], body: Option[Source[ByteString, _]]): Future[Result] = {
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer
    implicit val ev = env
    (body match {
      case None => handleError("no body", isAdminApiRoute = false)
      case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
        val bodyJson = bodyRaw.utf8String.parseJson

        val keypairPubKey = bodyJson.select("pubKey").asOpt[String]
        val keypairPrivKey = bodyJson.select("privKey").asOpt[String]
        val tokenBody = bodyJson.select("token").asOpt[String]
        val attenuatorChecks = bodyJson.select("checks").asOpt[List[String]]
        val keypairRef = bodyJson.select("keypair_ref").asOpt[String]
        val biscuitForgeRef = bodyJson.select("forge_ref").asOpt[String]

        if (keypairPubKey.isDefined && keypairPrivKey.isDefined) {
          processTokenAttenuation(tokenBody, biscuitForgeRef, attenuatorChecks, keypairPubKey.get).flatMap {
            case Left(err) => Results.Ok(
              Json.obj(
                "status" -> "error",
                "done" -> false,
                "error" -> err
              )
            ).vfuture
            case Right(attenuatedToken) => Results.Ok(Json.obj(
              "done" -> true,
              "token" -> attenuatedToken.serialize_b64url(),
              "pubKey" -> keypairPubKey.get
            )).vfuture
          }
        } else {
          keypairRef match {
            case None => handleError("no keypair or keypair_ref provided", isAdminApiRoute = false)
            case Some(keyPairRef) =>
              env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keyPairRef)) match {
                case None => handleError("no keypair found", isAdminApiRoute = false)
                case Some(keypairDb) =>
                  processTokenAttenuation(tokenBody, biscuitForgeRef, attenuatorChecks, keypairDb.pubKey).flatMap {
                    case Left(err) => Results.Ok(
                      Json.obj(
                        "status" -> "error",
                        "done" -> false,
                        "error" -> err
                      )
                    ).vfuture
                    case Right(attenuatedToken) => Results.Ok(
                      Json.obj(
                        "done" -> true,
                        "token" -> attenuatedToken.serialize_b64url(),
                        "pubKey" -> keypairDb.pubKey
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

  def processTokenAttenuation(tokenBody: Option[String], forgeRef: Option[String], attenuatorChecks: Option[List[String]], pubKey: String, kpAlgo: String = "ED25519")(implicit env: Env, ec: ExecutionContext): Future[Either[String, Biscuit]] = {
    if (forgeRef.isDefined && forgeRef.nonEmpty) {
      forgeTokenFromForgeId(forgeRef.get).flatMap {
        case Left(err) => Left(s"got error during token generation from forge = ${err}").vfuture
        case Right(forgedToken) => {
          attenuatorChecks match {
            case None => Left("no checks config provided").vfuture
            case Some(biscuitChecksConfig) =>
              AttenuatorConfig(biscuitChecksConfig).attenuate(forgedToken) match {
                case Left(err) => Left(s"Error during token attenuation : ${err}").vfuture
                case Right(attenuatedToken) => Right(attenuatedToken).vfuture
              }
          }
        }
      }
    } else {
      if (tokenBody.isDefined && pubKey.nonEmpty) {
        val publicKey = new PublicKey(BiscuitUtils.getAlgo(kpAlgo), pubKey)

        extractTokenFromBody(tokenBody, publicKey) match {
          case Left(extractionError) => Left(extractionError).vfuture
          case Right(biscuitToken) =>
            attenuatorChecks match {
              case None => Left("no checks config provided").vfuture
              case Some(biscuitChecksConfig) =>
                AttenuatorConfig(biscuitChecksConfig).attenuate(biscuitToken) match {
                  case Left(_) => Left("no checks config provided").vfuture
                  case Right(attenuatedToken) => Right(attenuatedToken).vfuture
                }
            }
        }
      } else {
        Left("missing token or public key").vfuture
      }
    }
  }

  def forgeTokenFromForgeId(forgeId: String)(implicit env: Env, ec: ExecutionContext): Future[Either[String, Biscuit]] = {
    env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitTokenForge(forgeId)) match {
      case None => Left("forge entity doesn't exist").vfuture
      case Some(forge) => {
        env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(forge.keypairRef)) match {
          case None => Left("forge keypair is not provided").vfuture
          case Some(keypair) => {
            forge.forgeToken().flatMap {
              case Left(err) => Left(err).vfuture
              case Right(biscuitToken) => Right(biscuitToken).vfuture
            }
          }
        }
      }
    }
  }

  def extractTokenFromBody(tokenBody: Option[String], publicKey: PublicKey)(implicit env: Env): Either[String, Biscuit] = {
    tokenBody match {
      case None => Left("no token provided")
      case Some(token) =>
            val extractedToken = BiscuitExtractorConfig.replaceHeader(token)
            Try(Biscuit.from_b64url(extractedToken, publicKey)).toEither match {
              case Left(err: org.biscuitsec.biscuit.error.Error) => Left(BiscuitUtils.handleBiscuitErrors(err))
              case Left(err) => Left(s"Unable to deserialize Biscuit token : ${err.getMessage}")
              case Right(biscuitToken) => Right(biscuitToken)
            }
    }
  }

  override def assets(): Seq[AdminExtensionAssetRoute] = Seq(
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
             |    const LazyCodeInput = dependencies.Components.Inputs.LazyCodeInput;
             |    const BackOfficeServices = dependencies.BackOfficeServices;
             |
             |    ${biscuitKeyPairPage}
             |    ${biscuitVerifiersPage}
             |    ${biscuitAttenuatorsPage}
             |    ${biscuitTokenForgePage}
             |    ${biscuitRbacPoliciesPage}
             |    ${biscuitRemoteFactsLoaderPage}
             |    ${biscuitRevocation}
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
             |          title: 'Biscuit Keypairs',
             |          description: 'All your Biscuit Keypairs',
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
             |          title: 'Biscuit Forges',
             |          description: 'All your Biscuit Forges',
             |          link: '/extensions/cloud-apim/biscuit/biscuit-forges',
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
             |        {
             |          title: 'Biscuit Revoked Tokens',
             |          description: 'All your Biscuit Revoked tokens',
             |          link: '/extensions/cloud-apim/biscuit/revoked-tokens',
             |          display: () => true,
             |          icon: () => 'fa-ban'
             |        },
             |        ]
             |      }],
             |      features: [
             |        {
             |          title: 'Biscuit Keypairs',
             |          description: 'All your Biscuit Keypairs',
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
             |          title: 'Biscuit Forges',
             |          description: 'All your Biscuit Forges',
             |          link: '/extensions/cloud-apim/biscuit/biscuit-forges',
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
             |        {
             |          title: 'Biscuit Revoked Tokens',
             |          description: 'All your Biscuit Revoked tokens',
             |          link: '/extensions/cloud-apim/biscuit/revoked-tokens',
             |          display: () => true,
             |          icon: () => 'fa-ban'
             |        },
             |      ],
             |      sidebarItems: [
             |        {
             |          title: 'Biscuit Keypairs',
             |          text: 'All your Biscuit Keypairs',
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
             |          title: 'Biscuit Forges',
             |          text: 'All your Biscuit Forges',
             |          path: 'extensions/cloud-apim/biscuit/biscuit-forges',
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
             |          path: '/extensions/cloud-apim/biscuit/remote-facts',
             |          icon: 'tower-broadcast'
             |        },
             |         {
             |          title: 'Biscuit Revoked Tokens',
             |          text: 'All your Biscuit Revoked tokens',
             |          path: '/extensions/cloud-apim/biscuit/revoked-tokens',
             |          icon: 'fa-ban'
             |        },
             |      ],
             |      searchItems: [
             |        {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/keypairs`
             |          },
             |          env: React.createElement('span', { className: "fas fa-key" }, null),
             |          label: 'Biscuit Keypairs',
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
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/biscuit-forges`
             |          },
             |          env: React.createElement('span', { className: "fas fa-hammer" }, null),
             |          label: 'Biscuit Forges',
             |          value: 'biscuit-forges',
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
             |        {
             |          action: () => {
             |            window.location.href = `/bo/dashboard/extensions/cloud-apim/biscuit/revoked-tokens`
             |          },
             |          env: React.createElement('span', { className: "fas fa-ban" }, null),
             |          label: 'Biscuit Revoked Token',
             |          value: 'biscuit-revoked-tokens',
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
             |          path: '/extensions/cloud-apim/biscuit/biscuit-forges/:taction/:titem',
             |          component: (props) => {
             |            return React.createElement(BiscuitTokenForge, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/biscuit-forges/:taction',
             |          component: (props) => {
             |            return React.createElement(BiscuitTokenForge, props, null)
             |          }
             |        },
             |        {
             |          path: '/extensions/cloud-apim/biscuit/biscuit-forges',
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
             |        },
             |         {
             |          path: '/extensions/cloud-apim/biscuit/revoked-tokens',
             |          component: (props) => {
             |            return React.createElement(BiscuitRevocation, props, null)
             |          }
             |        },
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
    // Generate a keypair from a body configuration
    AdminExtensionAdminApiRoute(
      "POST",
      "/api/extensions/biscuit/keypairs/_generate",
      wantsBody = true,
      (ctx, request, apk, body) => {
        implicit val ec = env.otoroshiExecutionContext
        implicit val mat = env.otoroshiMaterializer
        implicit val ev = env
        (body match {
          case None => handleError("no body provided", isAdminApiRoute = true)
          case Some(bodySource) =>
            bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
              val bodyJson = bodyRaw.utf8String.parseJson

              val algoInput = bodyJson.select("algorithm").asOpt[String].getOrElse("ED25519")

              val algo = algoInput.toUpperCase match {
                case "ED25519" => "Ed25519"
                // case "SECP256R1" => "SECP256R1" -- waiting for support in java lib
                case _ => "Ed25519"
              }

              val pkAlgo = algo.toUpperCase match {
                case "ED25519" => biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519
                //      case "SECP256R1" => biscuit.format.schema.Schema.PublicKey.Algorithm.SECP256R1 -- waiting for support in java lib
                case _ => biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519
              }

              val generatedKeyPair = KeyPair.generate(pkAlgo)
              val pubKey = generatedKeyPair.public_key().toHex.toUpperCase
              val privKey = generatedKeyPair.toHex.toUpperCase

              Results.Ok(
                Json.obj(
                  "algorithm" -> algo,
                  "pubKey" -> pubKey,
                  "privKey" -> privKey,
                  "algoPubKey" -> generatedKeyPair.public_key().toString
                )
              ).vfuture
            }
        })
      }
    ),
    // Routes for tokens revocation
    AdminExtensionAdminApiRoute(
      "POST",
      "/api/extensions/biscuit/tokens/revocation/_revoke",
      wantsBody = true,
      (ctx, request, apk, body) => {
        implicit val ev = env
        implicit val ec = env.otoroshiExecutionContext
        implicit val mat = env.otoroshiMaterializer

        (body match {
          case None => handleError("no body provided", isAdminApiRoute = true)
          case Some(bodySource) =>
            bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
              val bodyJson = bodyRaw.utf8String.parseJson

              val tokensToRevoke = bodyJson.asOpt[Seq[JsValue]].getOrElse(Seq.empty)
              var tokensRevoked = Seq.empty[String]

              tokensToRevoke.foreach { tok =>
                RevokedToken.format.reads(tok) match {
                  case JsSuccess(token, _) => {
                    if (token.revocationId.nonEmpty) {
                      tokensRevoked = tokensRevoked :+ token.revocationId
                      datastores.biscuitRevocationDataStore.add(
                        token.revocationId,
                        token.reason.some
                      )
                    }
                  }
                  case _ => ()
                }
              }

              Results.Ok(
                Json.obj(
                  "total_revoked" -> tokensRevoked.size,
                  "revoked" -> tokensRevoked,
                  "message" -> s"${tokensRevoked.size} token(s) revoked"
                )
              ).vfuture
            }
        })
      }
    ),
    AdminExtensionAdminApiRoute(
      "GET",
      "/api/extensions/biscuit/tokens/revocation/_all",
      wantsBody = false,
      (ctx, request, apk, body) => {
        implicit val ev = env
        implicit val ec = env.otoroshiExecutionContext

        env.adminExtensions.extension[BiscuitExtension].get.datastores.biscuitRevocationDataStore.list().map {
          tokens =>
            Results.Ok(
              Json.obj(
                "tokens" -> tokens.map(_.json)
              )
            )
        }
      }
    ),
    // Generate a token from a body
    AdminExtensionAdminApiRoute(
      "POST",
      "/api/extensions/biscuit/tokens/_generate",
      wantsBody = true,
      (ctx, request, apk, body) => {
        generateTokenFromBody(body, isAdminApiRoute = true)
      }
    ),
    // Generate a token from a forge entity
    AdminExtensionAdminApiRoute(
      "POST",
      "/api/extensions/biscuit/biscuit-forges/:id/_generate",
      wantsBody = false,
      (ctx, request, apk, body) => {
        implicit val ev = env
        implicit val ec = env.otoroshiExecutionContext

        ctx.named("id") match {
          case None => Results.NotFound(Json.obj("error" -> "Path parameter 'id' is not found")).vfuture
          case Some(forgeId) => {
            env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.biscuitTokenForge(forgeId)) match {
              case None => Results.NotFound(Json.obj("error" -> "Forge not found")).vfuture
              case Some(forge) => {
                val remoteCtx = Json.obj(
                  "snowflake" -> JsNull,
                  "backend" -> JsNull,
                  "apikey" -> apk.lightJson,
                  "user" -> JsNull,
                  "raw_request" -> JsonHelpers.requestToJson(request, TypedMap.empty),
                  "config" -> Json.obj(),
                  "global_config" -> Json.obj(),
                  "attrs" -> Json.obj(),
                  "phase" -> "admin_api_token_forge"
                )
                forge.forgeToken(remoteCtx).flatMap {
                  case Left(err) => handleError(err, isAdminApiRoute = true)
                  case Right(token) => {
                    Results.Ok(
                      Json.obj(
                        "token" -> token.serialize_b64url()
                      )
                    ).vfuture
                  }
                }
              }
            }
          }
        }
      }
    ),
    // Verify a token from a body token and config
    AdminExtensionAdminApiRoute(
      "POST",
      "/api/extensions/biscuit/tokens/_verify",
      wantsBody = true,
      (ctx, request, apk, body) => {
        verifyTokenFromBody(body, isAdminApiRoute = true)
      }
    ),
    // Verify a token from a verifier entity
    AdminExtensionAdminApiRoute(
      "POST",
      "/api/extensions/biscuit/biscuit-verifiers/:id/_verify",
      wantsBody = true,
      (ctx, request, apk, body) => {
        implicit val ev = env
        implicit val ec = env.otoroshiExecutionContext
        implicit val mat = env.otoroshiMaterializer

        ctx.named("id") match {
          case None => Results.NotFound(Json.obj("error" -> "Path parameter 'id' is not found")).vfuture
          case Some(verifierId) => {
            env.adminExtensions.extension[BiscuitExtension].get.states.biscuitVerifier(verifierId) match {
              case None => Results.NotFound(Json.obj("error" -> "The verifier entity is not found")).vfuture
              case Some(verifier) => {
                body match {
                  case None => Results.NotFound(Json.obj("error" -> "body is empty")).vfuture
                  case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
                    val bodyJson = bodyRaw.utf8String.parseJson
                    bodyJson.select("token").asOpt[String] match {
                      case None => Results.NotFound(Json.obj("error" -> "Token not provided")).vfuture
                      case Some(token) => {
                        env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(verifier.keypairRef)) match {
                          case None => Results.NotFound(Json.obj("error" -> "No keypair found in verifier entity")).vfuture
                          case Some(keypairDb) => {
                            verifyWithTokenInput(verifier.keypairRef, token, verifier.config, isAdminApiRoute = true)
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
    ),
    // Attenuate a token from a body token and checks config
    AdminExtensionAdminApiRoute(
      "POST",
      "/api/extensions/biscuit/tokens/_attenuate",
      wantsBody = true,
      (ctx, request, apk, body) => {
        attenuateTokenFromBody(body, isAdminApiRoute = true)
      }
    ),
    // Attenuate a token from an attenuator entity
    AdminExtensionAdminApiRoute(
      "POST",
      "/api/extensions/biscuit/biscuit-attenuators/:id/_attenuate",
      wantsBody = true,
      (ctx, request, apk, body) => {
        implicit val ev = env
        implicit val ec = env.otoroshiExecutionContext
        implicit val mat = env.otoroshiMaterializer

        ctx.named("id") match {
          case None => Results.NotFound(
            Json.obj(
              "status" -> "error",
              "error" -> "Path parameter 'id' is not found"
            )
          ).vfuture
          case Some(attenuatorId) => {
            env.adminExtensions.extension[BiscuitExtension].get.states.biscuitAttenuator(attenuatorId) match {
              case None => Results.NotFound(
                Json.obj(
                  "status" -> "error",
                  "error" -> "The attenuator entity is not found"
                )
              ).vfuture
              case Some(attenuator) => {
                body match {
                  case None => Results.NotFound(
                    Json.obj(
                      "status" -> "error",
                      "error" -> "body is empty"
                    )
                  ).vfuture
                  case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
                    val bodyJson = bodyRaw.utf8String.parseJson
                    bodyJson.select("token").asOpt[String] match {
                      case None => Results.NotFound(
                        Json.obj(
                          "status" -> "error",
                          "error" -> "Token not provided"
                        )
                      ).vfuture
                      case Some(token) => {

                        env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(attenuator.keypairRef)) match {
                          case None => Results.NotFound(
                            Json.obj(
                              "status" -> "error",
                              "error" -> "No keypair found in attenuator entity"
                            )
                          ).vfuture
                          case Some(keypairDb) => {

                            processTokenAttenuation(token.some, None, attenuator.config.checks.toList.some, keypairDb.pubKey).flatMap {
                              case Left(err) => Results.BadRequest(
                                Json.obj(
                                  "status" -> "error",
                                  "error" -> err
                                )
                              ).vfuture
                              case Right(attenuatedToken) => Results.Ok(
                                Json.obj(
                                  "status" -> "success",
                                  "message" -> "Token attenuated successfully",
                                  "token" -> attenuatedToken.serialize_b64url()
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
          }
        }
      }
    )
  )

  def attenuateTokenFromBody(body: Option[Source[ByteString, _]], isAdminApiRoute: Boolean): Future[Result] = {
    implicit val ev = env
    implicit val ec = env.otoroshiExecutionContext
    implicit val mat = env.otoroshiMaterializer

    (body match {
      case None => handleError("no body", isAdminApiRoute)
      case Some(bodySource) => bodySource.runFold(ByteString.empty)(_ ++ _).flatMap { bodyRaw =>
        val bodyJson = bodyRaw.utf8String.parseJson

        val keypairPubKey = bodyJson.select("pubKey").asOpt[String]
        val keypairPrivKey = bodyJson.select("privKey").asOpt[String]
        val tokenBody = bodyJson.select("token").asOpt[String]
        val attenuatorChecks = bodyJson.select("checks").asOpt[List[String]]
        val keypairRef = bodyJson.select("keypair_ref").asOpt[String]

        if (keypairPubKey.isDefined && keypairPrivKey.isDefined) {
          processTokenAttenuation(tokenBody, None, attenuatorChecks, keypairPubKey.get).flatMap {
            case Left(err) => Results.Ok(
              Json.obj(
                "error" -> err,
                "status" -> "error"
              )
            ).vfuture
            case Right(attenuatedToken) => Results.Ok(
              Json.obj(
                "status" -> "success",
                "message" -> "Token attenuated successfully",
                "token" -> attenuatedToken.serialize_b64url()
              )
            ).vfuture
          }
        } else {
          keypairRef match {
            case None => handleError("no keypair or keypair_ref provided", isAdminApiRoute)
            case Some(keyPairRef) =>
              env.adminExtensions.extension[BiscuitExtension].flatMap(_.states.keypair(keyPairRef)) match {
                case None => handleError("no keypair found", isAdminApiRoute)
                case Some(keypairDb) =>
                  processTokenAttenuation(tokenBody, None, attenuatorChecks, keypairDb.pubKey).flatMap {
                    case Left(err) => Results.Ok(Json.obj("error" -> err)).vfuture
                    case Right(attenuatedToken) => Results.Ok(
                      Json.obj(
                        "status" -> "success",
                        "message" -> "Token attenuated successfully",
                        "token" -> attenuatedToken.serialize_b64url()
                      )
                    ).vfuture
                  }
              }
          }
        }
      }
    }).recover {
      case e: Throwable => {
        if (isAdminApiRoute) {
          Results.InternalServerError(
            Json.obj(
              "status" -> "error",
              "error" -> e.getMessage
            )
          )
        } else {
          Results.Ok(
            Json.obj(
              "status" -> "error",
              "done" -> false,
              "error" -> e.getMessage
            )
          )
        }
      }
    }
  }
}
