import sbt._

object Dependencies {

  object V {
    val tessellation = "2.2.0"
    val decline = "2.4.1"
  }
  def tessellation(artifact: String): ModuleID = "org.constellation" %% s"tessellation-$artifact" % V.tessellation

  def decline(artifact: String = ""): ModuleID =
    "com.monovore" %% {
      if (artifact.isEmpty) "decline" else s"decline-$artifact"
    } % V.decline
  object Libraries {
    val tessellationKernel = tessellation("kernel")
    val tessellationDAGL1 = tessellation("dag-l1")
    val tessellationSDK = tessellation("sdk")
    val tessellationShared = tessellation("shared")
    val tessellationKeytool = tessellation("keytool")
    val tessellationCurrencyL0 = tessellation("currency-l0")
    val tessellationCurrencyL1 = tessellation("currency-l1")
    val declineCore = decline()
    val declineEffect = decline("effect")
    val declineRefined = decline("refined")
    val borerCore = "io.bullet" %% "borer-core" % "1.8.0"
    val borerDerivation = "io.bullet" %% "borer-derivation" % "1.8.0"
    val borerAkka = "io.bullet" %% "borer-compat-akka" % "1.8.0"
    val borerCirce = "io.bullet" %% "borer-compat-circe" % "1.8.0"
    val borerScodec = "io.bullet" %% "borer-compat-scodec" % "1.8.0"
  }


  // Scalafix rules
  val organizeImports = "com.github.liancheng" %% "organize-imports" % "0.5.0"

  object CompilerPlugin {

    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % "0.3.1"
    )

    val kindProjector = compilerPlugin(
      ("org.typelevel" % "kind-projector" % "0.13.2").cross(CrossVersion.full)
    )

    val semanticDB = compilerPlugin(
      ("org.scalameta" % "semanticdb-scalac" % "4.7.1").cross(CrossVersion.full)
    )
  }
}
