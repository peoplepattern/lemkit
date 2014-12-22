import sbt._
import Keys._
import com.typesafe.sbt.SbtStartScript._
import sbtrelease.ReleasePlugin._
import com.typesafe.sbt.SbtScalariform._
import scoverage.ScoverageSbtPlugin._
import org.scoverage.coveralls.CoverallsPlugin._

object LemkitBuild extends Build {

  import Implicits._

  override val settings = super.settings ++ Seq(
    organization := "com.peoplepattern",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-Xlint",
      "-Xfatal-warnings",
      "-Ywarn-dead-code",
      "-language:_",
      "-target:jvm-1.7",
      "-encoding", "UTF-8"),
    scalaVersion := "2.10.4",
    initialCommands := "import com.peoplepattern.classify"
  )

  lazy val root = Project(id = "lemkit", base = file("."))
    .aggregate(lemkitModel, lemkitTrain)
    .settings(releaseSettings: _*)

  lazy val lemkitModel = Project(id = "lemkit-model", base = file("lemkit-model"))
    .dependsOnLib(
      "org.scalatest" %% "scalatest" % "2.2.1" % "test",
      "net.liftweb"   %% "lift-json" % "2.6-RC1")
    .settings(scalariformSettings: _*)
    .settings(instrumentSettings: _*)
    .settings(coverallsSettings: _*)

  lazy val lemkitTrain = Project(id = "lemkit-train", base = file("lemkit-train"))
    .dependsOnLib("org.scalatest" %% "scalatest" % "2.2.1" % "test")
    .dependsOn(lemkitModel)
    .settings(scalariformSettings: _*)
    .settings(instrumentSettings: _*)
    .settings(coverallsSettings: _*)
    .settings(startScriptForClassesSettings: _*)
    .configs(AllTest)
    .configs(VowpalTest)
    .configs(LibLinearTest)
    .settings(inConfig(AllTest)(Defaults.testTasks): _*)
    .settings(inConfig(VowpalTest)(Defaults.testTasks): _*)
    .settings(inConfig(LibLinearTest)(Defaults.testTasks): _*)
    .settings(
      testOptions in Test := Seq(Tests.Filter(unitFilter)),
      testOptions in AllTest := Seq(Tests.Filter(allFilter)),
      testOptions in VowpalTest := Seq(Tests.Filter(vowpalFilter)),
      testOptions in LibLinearTest := Seq(Tests.Filter(libLinearFilter)))

  def vowpalFilter(name: String): Boolean = name endsWith "VowpalSpec"
  def libLinearFilter(name: String): Boolean = name endsWith "LibLinearSpec"
  def unitFilter(name: String): Boolean = (name endsWith "Spec") && !vowpalFilter(name) && !libLinearFilter(name)
  def allFilter(name: String): Boolean = name endsWith "Spec"

  lazy val AllTest = config("all") extend(Test)
  lazy val VowpalTest = config("vowpal") extend(Test)
  lazy val LibLinearTest = config("liblinear") extend(Test)
}

object Implicits {
  implicit class SuperProject(project: Project) {
    def dependsOnLib(libs: sbt.ModuleID*) = project.settings(libraryDependencies ++= libs)
    def useResolver(rslvrs: sbt.Resolver*) = project.settings(resolvers ++= rslvrs)
  }
}
