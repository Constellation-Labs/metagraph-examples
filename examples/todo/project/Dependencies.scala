import sbt._

object Dependencies {

  object V {
    val tessellation = "2.8.0"
    val decline = "2.4.1"
    val cats = "2.9.0"
    val catsEffect = "3.4.2"
    val organizeImports = "0.5.0"
    val weaver = "0.8.1"
    val scalapbCirce = "0.13.0"
  }
  def tessellation(artifact: String): ModuleID = "org.constellation" %% s"tessellation-$artifact" % V.tessellation

  def decline(artifact: String = ""): ModuleID =
    "com.monovore" %% {
      if (artifact.isEmpty) "decline" else s"decline-$artifact"
    } % V.decline
  object Libraries {
    val tessellationNodeShared = tessellation("node-shared")
    val tessellationCurrencyL0 = tessellation("currency-l0")
    val tessellationCurrencyL1 = tessellation("currency-l1")
    val declineCore = decline()
    val declineEffect = decline("effect")
    val declineRefined = decline("refined")
    val cats = "org.typelevel" %% "cats-core" % V.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
    val catsEffectTestkit = "org.typelevel" %% "cats-effect-testkit" % V.catsEffect
    val weaverCats = "com.disneystreaming" %% "weaver-cats" % V.weaver
    val weaverDiscipline = "com.disneystreaming" %% "weaver-discipline" % V.weaver
    val weaverScalaCheck = "com.disneystreaming" %% "weaver-scalacheck" % V.weaver
    val organizeImports = "com.github.liancheng" %% "organize-imports" % V.organizeImports
    val scalapbCirce = "io.github.scalapb-json" %% "scalapb-circe" % V.scalapbCirce
    val scalapbCirceMacro = "io.github.scalapb-json" %% "scalapb-circe-macros" % V.scalapbCirce

    // for scalapb/scalapb.proto or anything from google/protobuf/*.proto
    val scalapbRuntime = "com.thesamet.scalapb" %% "scalapb-runtime" % "0.11.13" % "protobuf"
    val scalapbValidate = "com.thesamet.scalapb" %% "scalapb-validate-core" % scalapb.validate.compiler.BuildInfo.version % "protobuf"

    val pureconfigCore = "com.github.pureconfig" %% "pureconfig" % "0.17.4"
    val pureconfigCats = "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.4"
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
