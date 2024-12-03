import sbt.*

object Dependencies {

  object V {
    val tessellation = "2.12.0"
    val metakit = "0.1.0"
    val organizeImports = "0.5.0"
    val betterMonadicFor = "0.3.1"
    val kindProjector = "0.13.3"
    val semanticDB = "4.11.2"
  }

  def tessellation(artifact: String): ModuleID = "org.constellation" %% s"tessellation-$artifact" % V.tessellation

  object Libraries {
    val tessellationSdk = tessellation("sdk")
    val tessellationCurrencyL0 = tessellation("currency-l0")
    val tessellationCurrencyL1 = tessellation("currency-l1")
    val metakit = "io.constellationnetwork" %% "metakit" % V.metakit
    val organizeImports = "com.github.liancheng" %% "organize-imports" % V.organizeImports
  }

  object CompilerPlugin {

    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % V.betterMonadicFor
    )

    val kindProjector = compilerPlugin(
      ("org.typelevel" % "kind-projector" % V.kindProjector).cross(CrossVersion.full)
    )

    val semanticDB = compilerPlugin(
      ("org.scalameta" % "semanticdb-scalac" % V.semanticDB).cross(CrossVersion.full)
    )
  }
}
