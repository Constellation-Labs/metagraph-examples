import Dependencies.*
import flywaysbt.FlywayPlugin.autoImport.flywayUrl
import sbt.*

ThisBuild / organization := "my.weathermetagraph"
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / evictionErrorLevel := Level.Warn
ThisBuild / assemblyMergeStrategy := {
  case "logback.xml"                                       => MergeStrategy.first
  case x if x.contains("io.netty.versions.properties")     => MergeStrategy.discard
  case PathList(xs @ _*) if xs.last == "module-info.class" => MergeStrategy.first
  case x =>
    (Compile / assembly / assemblyMergeStrategy).value(x)
}

lazy val commonSettings = Seq(
  resolvers += Resolver.mavenLocal,
  resolvers += Resolver.githubPackages("abankowski", "http-request-signer"),
  scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info", "-language:reflectiveCalls"),
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
  buildInfoPackage := "my.weathermetagraph.buildinfo"
)

lazy val root = (project in file("."))
  .settings(
    name := "weather"
  )
  .aggregate(sharedData, currencyL0, currencyL1, dataL1)

lazy val sharedData = (project in file("modules/weather-shared-data"))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(FlywayPlugin)
  .settings(
    commonSettings,
    name := "weather-shared-data",
    flywayUrl := sys.env.getOrElse("METAGRAPH_WEATHER_DB_URL", "jdbc:postgresql://localhost:5432/postgres"),
    flywayUser := sys.env.getOrElse("METAGRAPH_WEATHER_DB_USR", "postgres"),
    flywayPassword := sys.env.getOrElse("METAGRAPH_WEATHER_DB_PWD", ""),
    flywayLocations := Seq("filesystem:modules/weather-shared-data/src/main/resources/db/migration"),
    Compile / compile := (Compile / compile).dependsOn(flywayMigrate).value,
    libraryDependencies ++= Seq(
      Libraries.metakit,
      Libraries.tessellationNodeShared,
      Libraries.doobieCore,
      Libraries.doobieHikari,
      Libraries.doobiePostgres,
      Libraries.postgres
    )
  )

lazy val currencyL0 = (project in file("modules/weather-currency-l0"))
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(sharedData)
  .settings(
    commonSettings,
    name := "weather-currency-l0",
    libraryDependencies ++= Seq(Libraries.tessellationCurrencyL0)
  )

lazy val currencyL1 = (project in file("modules/weather-currency-l1"))
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(sharedData)
  .settings(
    commonSettings,
    name := "weather-currency-l1",
    libraryDependencies ++= Seq(Libraries.tessellationCurrencyL1)
  )

lazy val dataL1 = (project in file("modules/weather-data-l1"))
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(sharedData)
  .settings(
    commonSettings,
    name := "weather-data-l1",
    libraryDependencies ++= Seq(Libraries.tessellationCurrencyL1)
  )
