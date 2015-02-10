name := "metarest"

version := "0.1.0"

description := "Scala Macros to generate RESTful models"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "com.github.pathikrit"

scalaVersion := "2.11.5"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:_")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "com.kifi" %% "json-annotation" % "0.2",
  "com.typesafe.play" %% "play-json" % "2.4.0-M2",
  "org.specs2" %% "specs2" % "2.4.1" % "test"
)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

seq(bintraySettings:_*)
