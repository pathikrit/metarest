name := "metarest"

version := "0.2.1"

description := "Scala macros to generate RESTy models"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "com.github.pathikrit"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.4", "2.11.5")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:_")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.typesafeRepo("releases")
)

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _)

libraryDependencies ++= (
  if (scalaVersion.value.startsWith("2.10")) List("org.scalamacros" %% "quasiquotes" % "2.0.1") else Nil
)

libraryDependencies ++= Seq(
  "com.kifi" %% "json-annotation" % "0.2" % Test,
  "com.typesafe.play" %% "play-json" % "2.3.1" % Test,
  "org.specs2" %% "specs2" % "2.4.1" % Test
)

unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile, scalaBinaryVersion){
  (sourceDir, version) => sourceDir / (if (version.startsWith("2.10")) "scala_2.10" else "scala_2.11")
}

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

seq(bintraySettings:_*)
