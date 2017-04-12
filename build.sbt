name := "metarest"
version := "2.0.0"
description := "Scala macros to generate RESTy models"
organization := "com.github.pathikrit"
scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.11.8")
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros")
resolvers ++= Seq(Resolver.typesafeRepo("releases"))
libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _)
libraryDependencies ++= Seq(
  "org.scalameta" %% "scalameta" % "1.6.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)
addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M7" cross CrossVersion.full)
