val commonSettings = Seq(
  organization := "org.julienrf",
  version := "1.0.0-SNAPSHOT",
  scalaVersion := "2.11.5"
)

val forms = project
  .settings(commonSettings: _*)
  .settings(
    resolvers += "JTO snapshots" at "https://raw.github.com/jto/mvn-repo/master/snapshots",
    libraryDependencies ++= Seq(
      "io.github.jto" %% "validation-core" % "1.0-1c770f4",
      "com.scalatags" %% "scalatags" % "0.4.2",
      "com.typesafe.play" %% "play" % "2.3.7",
      "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
    )
  )

val example = project
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "ch.epfl.lamp" %% "scala-records" % "0.3"
  )
  .dependsOn(forms)

val `play-forms` = project.in(file("."))
  .settings(commonSettings: _*)
  .dependsOn(forms)
  .aggregate(forms)