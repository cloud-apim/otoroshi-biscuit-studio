import Dependencies.*

ThisBuild / scalaVersion     := "2.12.13"
ThisBuild / version          := "1.0.0-dev"
ThisBuild / organization     := "com.cloud-apim"
ThisBuild / organizationName := "Cloud-APIM"

Test / parallelExecution := false

lazy val excludesJackson         = Seq(
  ExclusionRule(organization = "com.fasterxml.jackson.core"),
  ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
  ExclusionRule(organization = "com.fasterxml.jackson.dataformat")
)

lazy val scalaExclusion  = Seq(
  ExclusionRule(organization = "scala"),
  ExclusionRule(organization = "org.scala-lang"),
)

lazy val root = (project in file("."))
  .settings(
    name := "otoroshi-biscuit-studio",
    libraryDependencies ++= Seq(
      "fr.maif" %% "otoroshi" % "17.9.1" % "provided" excludeAll (scalaExclusion: _*),
      "org.biscuitsec" % "biscuit" % "4.0.1", // biscuit spec 3.2
      "com.arakelian" % "java-jq" % "1.3.0" % Test excludeAll (excludesJackson: _*),
      "org.scala-lang" % "scala-reflect" % "2.12.13" % Test,
      munit % Test
    ),
    assembly / test  := {},
    assembly / assemblyJarName := "otoroshi-biscuit-studio-assembly_2.12-dev.jar",
  )
