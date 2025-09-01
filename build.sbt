import Dependencies.*

ThisBuild / scalaVersion     := "2.12.13"
ThisBuild / version          := "1.0.0-dev"
ThisBuild / organization     := "com.cloud-apim"
ThisBuild / organizationName := "Cloud-APIM"

Test / parallelExecution := false

lazy val scalaExclusion  = Seq(
  ExclusionRule(organization = "scala"),
  ExclusionRule(organization = "org.scala-lang"),
)

lazy val root = (project in file("."))
  .settings(
    name := "otoroshi-biscuit-studio",
    libraryDependencies ++= Seq(
      "fr.maif" %% "otoroshi" % "17.5.1" % "provided" excludeAll (scalaExclusion: _*),
      "org.biscuitsec" % "biscuit" % "4.0.1", // biscuit spec 3.2
      munit % Test
    ),
    assembly / test  := {},
    assembly / assemblyJarName := "otoroshi-biscuit-studio-assembly_2.12-dev.jar",
  )
