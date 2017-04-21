name := "metarest"
version := "2.0.0"
description := "Scala macros to generate RESTy models"
organization := "com.github.pathikrit"
scalaVersion := "2.12.1"
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
libraryDependencies ++= Seq(
  "org.scalameta" %% "scalameta" % "1.7.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)
addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M8" cross CrossVersion.full)
