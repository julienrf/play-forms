val commonSettings = Seq(
  organization := "org.julienrf",
  scalaVersion := "2.11.7",
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

val publishSettings = commonSettings ++ Seq(
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
  useGpg := true
)

val `play-forms` = project.in(file("play-forms"))
  .enablePlugins(GitVersioning)
  .settings(publishSettings)
  .settings(
    name := "play-forms",
    resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases",
    libraryDependencies ++= Seq(
      component("play"),
      "org.scalacheck" %% "scalacheck" % "1.12.1" % Test
    ),
    homePage := Path.userHome / "sites" / "julienrf.github.com",
    publishDoc := {
      IO.copyDirectory((doc in Compile).value, homePage.value / "play-forms" / version.value / "api")
    },
    scalacOptions in (Compile, doc) += "-groups"
  )

val `play-forms-twirl` = project.in(file("play-forms-twirl"))
  .enablePlugins(SbtTwirl, GitVersioning)
  .settings(publishSettings)
  .settings(
    name := "play-forms-twirl",
    libraryDependencies ++= Seq(
      component("play"),
      "org.scalacheck" %% "scalacheck" % "1.12.1" % Test
    )
  )
  .dependsOn(`play-forms`)

val `play-forms-scalatags` = project.in(file("play-forms-scalatags"))
  .enablePlugins(GitVersioning)
  .settings(publishSettings)
  .settings(
    name := "play-forms-scalatags",
    libraryDependencies ++= Seq(
      "com.scalatags" %% "scalatags" % "0.4.2",
      component("play"),
      "org.scalacheck" %% "scalacheck" % "1.12.1" % Test
    )
  )
  .dependsOn(`play-forms`)

val example = project.in(file("example"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(commonSettings: _*)
  .dependsOn(`play-forms-twirl`)

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
  .dependsOn(`play-forms-twirl`, `manual-macros`)

val `play-forms-project` = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "play-forms-project"
  )
  .aggregate(`play-forms`, `play-forms-twirl`, `play-forms-scalatags`)

lazy val homePage = settingKey[File]("Path to the project home page")

lazy val publishDoc = taskKey[Unit]("Publish the documentation")
