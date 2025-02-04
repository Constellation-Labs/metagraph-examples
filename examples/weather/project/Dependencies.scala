import sbt.*

object Dependencies {

  object V {
    val tessellation = "2.12.0"
    val metakit = "0.0.2"
    val doobie = "1.0.0-RC6"
    val http4s = "0.23.16"
    val decline = "2.4.1"
  }

  def tessellation(
    artifact: String
  ): ModuleID = "org.constellation" %% s"tessellation-$artifact" % V.tessellation

  def doobie(
    artifact: String
  ): ModuleID = "org.tpolecat" %% s"doobie-$artifact" % V.doobie

  def http4s(
    artifact: String
  ): ModuleID = "org.http4s" %% s"http4s-$artifact" % V.http4s

  def decline(
    artifact: String = ""
  ): ModuleID =
    "com.monovore" %% {
      if (artifact.isEmpty) "decline" else s"decline-$artifact"
    } % V.decline

  object Libraries {
    val tessellationNodeShared = tessellation("node-shared")
    val tessellationCurrencyL0 = tessellation("currency-l0")
    val tessellationCurrencyL1 = tessellation("currency-l1")
    val doobieCore = doobie("core")
    val doobieHikari = doobie("hikari")
    val doobiePostgres = doobie("postgres")
    val http4sDsl = http4s("dsl")
    val http4sCirce = http4s("circe")
    val http4sServer = http4s("ember-server")
    val http4sClient = http4s("ember-client")
    val declineCore = decline()
    val declineEffect = decline("effect")
    val declineRefined = decline("refined")
    val metakit = "io.constellationnetwork" %% "metakit"    % V.metakit
    val postgres = "org.postgresql"          % "postgresql" % "42.7.5"
  }
}
