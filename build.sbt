import CustomKeys._
import Dependencies._

//updateOptions := updateOptions.value.withLatestSnapshots(true)

ThisBuild / scalaVersion     := "3.1.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "it.unich.scalafixexamples"
ThisBuild / organizationName := "it.unich"

ThisBuild / resolvers ++= Seq (
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

ThisBuild / pplJar := {
  try {
    val PPLPathName = scala.sys.process.Process("ppl-config -l").lineStream.head+"/ppl/ppl_java.jar"
    if (file(PPLPathName).exists) Some(PPLPathName) else None
  } catch {
    case _ : Exception => None
  }
}

ThisBuild / fork := true

lazy val root = (project in file("."))
  .settings(
    name := "ScalaFixExamples",
    libraryDependencies += "it.unich.scalafix" %% "scalafix" % "0.9.0-SNAPSHOT",
    libraryDependencies += "it.unich.scalafix" % "jppl" % "0.2-SNAPSHOT",
    Compile / unmanagedJars ++= pplJar.value.toSeq map file
  )
