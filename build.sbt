import Dependencies._

ThisBuild / scalaVersion     := "2.12.15"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val AkkaHttpVersion = "10.1.11"
lazy val AkkaVersion = "2.6.9"

lazy val root = (project in file("."))
.enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    name := "oldAkkaHttp",
    // dockerExposedPorts ++= Seq(9000, 9443),
    libraryDependencies += scalaTest % Test,
    libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
),
  libraryDependencies += "io.monix" %% "monix" % "3.2.2",
  libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.0",
  libraryDependencies +=   "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
   libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion,
   libraryDependencies += "org.scalatestplus" %% "scalacheck-1-15" % "3.2.10.0" % "test",
   libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"
  )

scalacOptions += "-Ypartial-unification"


// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
