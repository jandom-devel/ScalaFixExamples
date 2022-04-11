import Dependencies._

//updateOptions := updateOptions.value.withLatestSnapshots(true)

//ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / scalaVersion     := "2.12.15"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "it.unich.scalafixexamples"
ThisBuild / organizationName := "it.unich"

ThisBuild / resolvers ++= Seq (
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val root = (project in file("."))
  .settings(
    name := "ScalaFixExamples",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "it.unich.scalafix" %% "scalafix" % "0.7.0",
    libraryDependencies += "it.unich.scalafix" % "jppl" % "0.2-SNAPSHOT",
    
    Compile/unmanagedJars += file("/usr/local/lib/ppl/ppl_java.jar")
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
