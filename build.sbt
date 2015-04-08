val commonSettings = Seq(
  organization := "org.julienrf",
  version := "0.0.0-SNAPSHOT",
  scalaVersion := "2.11.6",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-language:existentials"
  )
)

val `play-forms` = project.in(file("forms"))
  .settings(commonSettings: _*)
  .settings(
    name := "play-forms",
    resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases",
    libraryDependencies ++= Seq(
      "com.scalatags" %% "scalatags" % "0.4.2",
      component("play"),
      "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
    ),
    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org"
      if (isSnapshot.value) Some("snapshots" at s"$nexus/content/repositories/snapshots")
      else Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
    },
    pomExtra := (
      <url>http://github.com/julienrf/play-forms</url>
      <licenses>
        <license>
          <name>MIT License</name>
          <url>http://opensource.org/licenses/mit-license.php</url>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:julienrf/play-forms.git</url>
        <connection>scm:git:git@github.com:julienrf/play-forms.git</connection>
      </scm>
      <developers>
        <developer>
          <id>julienrf</id>
          <name>Julien Richard-Foy</name>
          <url>http://julien.richard-foy.fr</url>
        </developer>
      </developers>
    ),
    useGpg := true,
    homePage := Path.userHome / "sites" / "julienrf.github.com",
    publishDoc := {
      IO.copyDirectory((doc in Compile).value, homePage.value / "play-forms" / version.value / "api")
    },
    scalacOptions in (Compile, doc) += "-groups"
  )

val example = project.in(file("example"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(commonSettings: _*)
  .dependsOn(`play-forms`)

val `manual-macros` = project.in(file("manual-macros"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    scalacOptions ++= Seq(/*"-Ymacro-debug-lite"*/)
  )

val manual = project.in(file("manual"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(commonSettings: _*)
  .settings(
    scalacOptions ++= Seq("-Yrangepos"/*, "-Ymacro-debug-lite"*/),
    libraryDependencies += "org.pegdown" % "pegdown" % "1.5.0",
    herokuAppName in Compile := "play-forms-doc",
    herokuProcessTypes in Compile := Map(
      "web" -> "target/universal/stage/bin/manual -Dhttp.port=$PORT"
    ),
    herokuSkipSubProjects in Compile := false
  )
  .dependsOn(`play-forms`, `manual-macros`)

val `play-forms-project` = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "play-forms-project",
    includeFilter in (Assets, LessKeys.less) := "style.less"
  ).dependsOn(`play-forms`)
  .aggregate(`play-forms`)

lazy val homePage = settingKey[File]("Path to the project home page")

lazy val publishDoc = taskKey[Unit]("Publish the documentation")
