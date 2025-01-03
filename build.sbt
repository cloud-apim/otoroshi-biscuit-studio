import Dependencies.*

ThisBuild / scalaVersion     := "2.12.13"
ThisBuild / version          := "1.0.0-dev"
ThisBuild / organization     := "com.cloud-apim"
ThisBuild / organizationName := "Cloud-APIM"

lazy val jackson = Seq(
  ExclusionRule("com.fasterxml.jackson.core", "jackson-databind"),
  ExclusionRule("io.opentelemetry"),
)

lazy val slf4j = Seq(
  ExclusionRule("org.slf4j"),
  ExclusionRule("ch.qos.logback")
)

lazy val netty = Seq(
  ExclusionRule("io.netty", "netty-transport-native-epoll"),
  ExclusionRule("io.netty", "netty-transport-native-kqueue"),
)

lazy val all = jackson ++ slf4j

lazy val root = (project in file("."))
  .settings(
    name := "otoroshi-biscuit-studio",
    resolvers ++= Seq(
      "jitpack" at "https://jitpack.io",
      "spring-milestones" at "https://repo.spring.io/milestone",
      "spring-snapshots" at "https://repo.spring.io/snapshot"
    ),
    libraryDependencies ++= Seq(
      "fr.maif" %% "otoroshi" % "16.19.0" % "provided" excludeAll (netty: _*),
      "io.netty" % "netty-transport-native-kqueue" % "4.1.107.Final" % "provided" excludeAll(jackson: _*),
      "io.netty" % "netty-transport-native-epoll" % "4.1.107.Final" % "provided" excludeAll(jackson: _*),
      "org.biscuitsec" % "biscuit" % "4.0.0",
      munit % Test
    ),
    assembly / test  := {},
    assembly / assemblyJarName := "otoroshi-biscuit-studio-assembly_2.12-dev.jar",
    assembly / assemblyMergeStrategy := {
      case PathList(ps @ _*) if ps.contains("module-info.class") => MergeStrategy.first
      case PathList(ps @ _*) if ps.last == "FastDoubleParser-NOTICE" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last == "groovy-release-info.properties" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last == "io.netty.versions.properties" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last == "additional-spring-configuration-metadata.json" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last == "spring-configuration-metadata.json" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last == "aot.factories" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last == "spring.factories" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last == "okio.kotlin_module" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last == "org.springframework.boot.autoconfigure.AutoConfiguration.imports" => MergeStrategy.first
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )