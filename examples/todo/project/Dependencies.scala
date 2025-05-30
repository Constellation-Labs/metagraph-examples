import sbt._

object Dependencies {

  object V {
    val metakit = "1.0.0-rc.3"
    val decline = "2.4.1"
    val cats = "2.9.0"
    val catsEffect = "3.4.2"
    val organizeImports = "0.5.0"
    val weaver = "0.8.1"
    val pureconfig = "0.17.4"
  }

  def decline(artifact: String = ""): ModuleID =
    "com.monovore" %% {
      if (artifact.isEmpty) "decline" else s"decline-$artifact"
    } % V.decline

  object Libraries {
    val metakit = "io.constellationnetwork" %% "metakit" % V.metakit
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
    val pureconfigCore = "com.github.pureconfig" %% "pureconfig" % V.pureconfig
    val pureconfigCats = "com.github.pureconfig" %% "pureconfig-cats-effect" % V.pureconfig
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
