name := "metarest"

version := "2.0.0"

description := "Scala macros to generate RESTy models"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "com.github.pathikrit"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.11.0", "2.11.1", "2.11.2", "2.11.4", "2.11.5")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros")

resolvers ++= Seq(
  Resolver.typesafeRepo("releases")
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % Test
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

seq(bintraySettings:_*)
