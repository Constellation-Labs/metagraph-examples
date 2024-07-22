addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.1")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.11")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.1")
addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.5.3")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.2.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.6") //https://github.com/thesamet/sbt-protoc

// https://index.scala-lang.org/scalapb/scalapb/artifacts/compilerplugin
// NOTE! This must be here and not in build.sbt otherwise the `scalapb.gen()` isn't seen
libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin"           % "0.11.13",
  "com.thesamet.scalapb" %% "scalapb-validate-codegen" % "0.3.4"
)

addDependencyTreePlugin
