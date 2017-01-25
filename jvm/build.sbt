// LEMKIT JVM

import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

lazy val commonSettings = Seq(
  organization := "com.peoplepattern",
  scalaVersion := "2.11.5",
  crossScalaVersions := Seq("2.10.4", "2.11.5"),
  scalacOptions in ThisBuild ++= Seq(
    "-unchecked",
    "-feature",
    "-deprecation",
    "-language:_",
    "-Xlint",
    "-Xfatal-warnings",
    "-Ywarn-dead-code",
    "-target:jvm-1.7",
    "-encoding",
    "UTF-8"
  )
) ++ scalariformSettings

lazy val lemkitCore = project
  .in(file("lemkit-core"))
  .settings(
    name := "lemkit-core",
    organization := "com.peoplepattern",
    description := "Core data structures for linear classification",
    publishMavenStyle := true,
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "it.unimi.dsi" % "fastutil" % "7.0.13",
      "com.eclipsesource.minimal-json" % "minimal-json" % "0.9.4",
      "com.novocode" % "junit-interface" % "0.11" % "test"),
    javacOptions in compile ++= Seq("-Xlint:all", "-Xdiags:verbose"),
    javacOptions in doc ++= Seq(
      "-link", "http://docs.oracle.com/javase/7/docs/api",
      "-public"),
    bintrayOrganization := Some("peoplepattern"),
    bintrayReleaseOnPublish := true,
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("http://peoplepattern.github.io/lemkit"))
  )

lazy val lemkitTrain = project
  .in(file("lemkit-train"))
  .settings(
    name := "lemkit-train",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
  )
  .settings(commonSettings: _*)
  .settings(
    bintrayOrganization := Some("peoplepattern"),
    bintrayReleaseOnPublish := true,
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("http://peoplepattern.github.io/lemkit")))
  .dependsOn(lemkitCore)

lazy val root = project
  .in(file("."))
  .settings(name := "lemkit")
  .settings(commonSettings: _*)
  .settings(
    publish := { },
    bintrayUnpublish := { })
  .aggregate(lemkitCore, lemkitTrain)
