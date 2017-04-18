name := """root"""

version := "0.1"

lazy val circeVersion = "0.5.1"
lazy val http4sVersion = "0.14.6"

val commonSettings = Seq(
  organization := "com.github.gvolpe",
  version := "0.1",
  licenses +=("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/gvolpe/http4s-auth")),
  //releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scalatest"   %% "scalatest"            % "2.2.4"       % "test",
    "com.gilt"        %% "gfc-timeuuid"         % "0.0.8",
    "org.reactormonk" %% "cryptobits"           % "1.1",
    "org.http4s"      %% "http4s-dsl"           % http4sVersion,
    "org.http4s"      %% "http4s-blaze-server"  % http4sVersion,
    "org.http4s"      %% "http4s-blaze-client"  % http4sVersion,
    "org.http4s"      %% "http4s-circe"         % http4sVersion,
    "io.circe"        %% "circe-core"           % circeVersion,
    "io.circe"        %% "circe-generic"        % circeVersion,
    "io.circe"        %% "circe-parser"         % circeVersion,
    "ch.qos.logback"  %  "logback-classic" % "1.0.6" % "runtime"
  ),
  resolvers += "Apache public" at "https://repository.apache.org/content/groups/public/",
  scalacOptions ++= Seq(
    "-Xlint"
    // "-Xfatal-warnings",
    // "-feature"
    // "-deprecation", //hard to handle when supporting multiple scala versions...
    // , "-Xlog-implicits"
    //"-Ydebug"
  ),
  incOptions := incOptions.value.withNameHashing(true),
  coverageExcludedPackages := "com\\.gvolpe\\.http4s\\.auth\\.demo.*",
  publishTo := {
    val sonatype = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at sonatype + "content/repositories/snapshots")
    else
      Some("releases" at sonatype + "service/local/staging/deploy/maven2")
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra :=
    <scm>
      <url>git@github.com:gvolpe/http4s-auth.git</url>
      <connection>scm:git:git@github.com:gvolpe/http4s-auth.git</connection>
    </scm>
      <developers>
        <developer>
          <id>gvolpe</id>
          <name>Gabriel Volpe</name>
          <url>http://github.com/gvolpe</url>
        </developer>
      </developers>
)

lazy val root = project.in(file("."))
  .aggregate(`http4s-auth`, `http4s-auth-examples`)

lazy val `http4s-auth` = project.in(file("auth-core"))
  .settings(commonSettings: _*)

lazy val `http4s-auth-examples` = project.in(file("auth-examples"))
  .settings(commonSettings: _*)
  .dependsOn(`http4s-auth`)

sonatypeProfileName := "com.github.gvolpe"

publishArtifact := false
