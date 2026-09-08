import Dependencies._

ThisBuild / scalaVersion     := "3.8.4"
ThisBuild / version          := "1.0.0-dev"
ThisBuild / organization     := "com.cloud-apim"
ThisBuild / organizationName := "Cloud-APIM"

Test / parallelExecution := false

lazy val excludesJackson         = Seq(
  ExclusionRule(organization = "com.fasterxml.jackson.core"),
  ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
  ExclusionRule(organization = "com.fasterxml.jackson.dataformat")
)

lazy val root = (project in file("."))
  .settings(
    name := "otoroshi-biscuit-studio",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Wunused:all",
      // the wasm4s "bundle" jar (transitive, provided) vendors an older scala 3 stdlib where
      // `scala.caps` is an object while scala-library 3.8.4 declares it as a package. otoroshi
      // itself silences the very same warning.
      "-Wconf:msg=package scala contains object and package with same name:s",
    ),
    libraryDependencies ++= Seq(
      "fr.maif" %% "otoroshi" % "18.0.0-preview6" % "provided",
      "org.biscuitsec" % "biscuit" % "4.0.1", // biscuit spec 3.2
      "com.arakelian" % "java-jq" % "1.3.0" % Test excludeAll (excludesJackson: _*),
      munit % Test
    ),
    assembly / test  := {},
    assembly / assemblyJarName := "otoroshi-biscuit-studio-assembly_3-dev.jar",
    // otoroshi already provides the exact same scala3-library at runtime, no need to ship a
    // second copy of the whole stdlib in the plugin jar
    assembly / assemblyPackageScala / assembleArtifact := false,
  )
