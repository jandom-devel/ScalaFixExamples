//updateOptions := updateOptions.value.withLatestSnapshots(true)

ThisBuild / scalaVersion     := "3.1.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "it.unich.scalafixexamples"
ThisBuild / organizationName := "it.unich"

ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("releases")
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")
ThisBuild / resolvers ++= Seq(Resolver.mavenLocal)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-language:adhocExtensions",
  "-source", "future"
)

ThisBuild / fork := true

lazy val root = (project in file("."))
  .settings(
    name := "ScalaFixExamples",
    libraryDependencies += "it.unich.scalafix" %% "scalafix" % "0.9.0",
    libraryDependencies += "it.unich.jppl" % "jppl" % "0.3",
  )
  .enablePlugins(JmhPlugin)
