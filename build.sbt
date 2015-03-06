name := "metarest"

version := "1.0.0-SNAPSHOT"

description := "Scala macros to generate RESTy models"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "com.github.pathikrit"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.4", "2.11.5")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros")

resolvers ++= Seq(
  Resolver.typesafeRepo("releases"),
  "bleibinha.us/archiva releases" at "http://bleibinha.us/archiva/repository/releases",
  "spray repo" at "repo.spray.io"
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _)

libraryDependencies ++= (
  if (scalaVersion.value.startsWith("2.10")) Seq("org.scalamacros" %% "quasiquotes" % "2.1.0-M5") else Nil
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % Test,
  "com.kifi" %% "json-annotation" % "0.1" % Test,
  "com.typesafe.play" %% "play-json" % "2.3.8" % Test,
  "us.bleibinha" %% "spray-json-annotation" % "0.4" % Test,
  "io.spray" %% "spray-json" % "1.3.1" % Test
)

unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile, scalaBinaryVersion) {
  (sourceDir, version) => sourceDir / s"scala_$version"
}

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

seq(bintraySettings:_*)
