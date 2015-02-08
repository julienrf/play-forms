val commonSettings = Seq(
  organization := "org.julienrf",
  version := "0.0.0-SNAPSHOT",
  scalaVersion := "2.11.5"
)

val `play-forms` = project.in(file("forms"))
  .settings(commonSettings: _*)
  .settings(
    name := "play-forms",
    resolvers += "JTO snapshots" at "https://raw.github.com/jto/mvn-repo/master/snapshots",
    libraryDependencies ++= Seq(
      "io.github.jto" %% "validation-core" % "1.0-1c770f4",
      "com.scalatags" %% "scalatags" % "0.4.2",
      "com.typesafe.play" %% "play" % "2.3.7",
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
    }
  )

val example = project.in(file("example"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "ch.epfl.lamp" %% "scala-records" % "0.3"
  )
  .dependsOn(`play-forms`)

val `play-forms-project` = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(name := "play-forms-project")
  .dependsOn(`play-forms`)
  .aggregate(`play-forms`)

lazy val homePage = settingKey[File]("Path to the project home page")

lazy val publishDoc = taskKey[Unit]("Publish the documentation")
