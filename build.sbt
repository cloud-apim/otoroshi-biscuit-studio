import Dependencies.*

ThisBuild / scalaVersion     := "2.12.13"
ThisBuild / version          := "1.0.0-dev"
ThisBuild / organization     := "com.cloud-apim"
ThisBuild / organizationName := "Cloud-APIM"

Test / parallelExecution := false

lazy val root = (project in file("."))
  .settings(
    name := "otoroshi-biscuit-studio",
    libraryDependencies ++= Seq(
      "fr.maif" %% "otoroshi" % "16.23.0" % "provided",
      "org.biscuitsec" % "biscuit" % "4.0.0",
      munit % Test
    ),
    assembly / test  := {},
    assembly / assemblyJarName := "otoroshi-biscuit-studio-assembly_2.12-dev.jar",
  )
