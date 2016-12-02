// LEMKIT JVM

import com.typesafe.sbt.SbtStartScript._

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

lazy val lkModel = project
  .in(file("lemkit-model"))
  .settings(commonSettings: _*)
  .settings(startScriptForClassesSettings: _*)
  .settings(
    name := "lemkit-model",
    libraryDependencies ++= Seq(
      "net.liftweb" %% "lift-json" % "2.6-RC1",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
  )

lazy val AllTest = config("all") extend (Test)
lazy val VowpalTest = config("vowpal") extend (Test)
lazy val LibLinearTest = config("liblinear") extend (Test)

def vowpalFilter(name: String): Boolean = name endsWith "VowpalSpec"
def libLinearFilter(name: String): Boolean = name endsWith "LibLinearSpec"
def unitFilter(name: String): Boolean =
  (name endsWith "Spec") && !vowpalFilter(name) && !libLinearFilter(name)
def allFilter(name: String): Boolean = name endsWith "Spec"

lazy val lkTrain = project
  .in(file("lemkit-train"))
  .settings(commonSettings: _*)
  .settings(startScriptForClassesSettings: _*)
  .settings(
    name := "lemkit-train",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
  )
  .configs(AllTest)
  .configs(VowpalTest)
  .configs(LibLinearTest)
  .settings(inConfig(AllTest)(Defaults.testTasks): _*)
  .settings(inConfig(VowpalTest)(Defaults.testTasks): _*)
  .settings(inConfig(LibLinearTest)(Defaults.testTasks): _*)
  .settings(testOptions in Test := Seq(Tests.Filter(unitFilter)),
            testOptions in AllTest := Seq(Tests.Filter(allFilter)),
            testOptions in VowpalTest := Seq(Tests.Filter(vowpalFilter)),
            testOptions in LibLinearTest := Seq(Tests.Filter(libLinearFilter)))
  .settings(startScriptForClassesSettings: _*)
  .dependsOn(lkModel)

lazy val root = project
  .in(file("."))
  .settings(name := "lemkit")
  .settings(commonSettings: _*)
  .aggregate(lkModel, lkTrain)
