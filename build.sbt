name := "metarest"

version := "0.3.0"

description := "Scala macros to generate RESTy models"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "com.github.pathikrit"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.4", "2.11.5")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:experimental.macros")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.typesafeRepo("releases")
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _)

libraryDependencies ++= (
  if (scalaVersion.value.startsWith("2.10")) List("org.scalamacros" %% "quasiquotes" % "2.0.1") else Nil
)

libraryDependencies ++= Seq(
  "com.kifi" %% "json-annotation" % "0.1" % Test,
  "com.typesafe.play" %% "play-json" % "2.3.7" % Test,
  "org.scalatest" %% "scalatest" % "2.2.1" % Test
)

unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile, scalaBinaryVersion) {
  (sourceDir, version) => sourceDir / s"scala_$version"
}

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

seq(bintraySettings:_*)
