import sbt._
import Keys._
import com.typesafe.sbt.SbtStartScript._
import sbtrelease.ReleasePlugin._
import com.typesafe.sbt.SbtScalariform._
//import scoverage.ScoverageSbtPlugin._
//import org.scoverage.coveralls.CoverallsPlugin._

object LemkitBuild extends Build {

  import Implicits._

  def scalacOptionsVersion(v: String) = {
    Seq(
      "-unchecked",
      "-deprecation",
      "-Xlint",
      "-Xfatal-warnings",
      "-Ywarn-dead-code",
      "-encoding", "UTF-8") ++ (
      CrossVersion.partialVersion(v) match {
        //case Some((2, scalaMajor)) if scalaMajor == 9 => Nil
        case Some((2, 9)) => Seq("-target:jvm-1.5")
        case _ => Seq("-target:jvm-1.7", "-language:_")
      }
      //if (v.startsWith("2.9")) Seq() else Seq("-language:_")
      )
  }

  override val settings = super.settings ++ Seq(
    organization := "com.peoplepattern",
    //scalacOptions ++= scalaVersion(scalacOptionsVersion),
    scalaVersion := "2.10.4",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    crossScalaVersions := Seq("2.10.4", "2.11.4"),
    // too hard to get 2.9 working
    // crossScalaVersions := Seq("2.9.2", "2.10.4", "2.11.4"),
    initialCommands := "import com.peoplepattern.classify"
  )

  lazy val root = Project(id = "lemkit", base = file("."))
    .aggregate(lemkitModel, lemkitTrain)
    .settings(releaseSettings: _*)

  def scalatestVersion(v: String) = {
    if (v.startsWith("2.9"))
      Seq("org.scalatest" %% "scalatest" % "2.0.M5b" % "test")
    else
      Seq("org.scalatest" %% "scalatest" % "2.2.3" % "test")
  }

  lazy val lemkitModel = Project(id = "lemkit-model", base = file("lemkit-model"))
    .dependsOnLib(
      "net.liftweb"   %% "lift-json" % "2.6-RC1")
    .settings(libraryDependencies <++= scalaVersion(scalatestVersion))
    .settings(scalariformSettings: _*)
    //.settings(instrumentSettings: _*)
    //.settings(coverallsSettings: _*)
    .settings(startScriptForClassesSettings: _*)

  lazy val lemkitTrain = Project(id = "lemkit-train", base = file("lemkit-train"))
    .settings(libraryDependencies <++= scalaVersion(scalatestVersion))
    .dependsOn(lemkitModel)
    .settings(scalariformSettings: _*)
    //.settings(instrumentSettings: _*)
    //.settings(coverallsSettings: _*)
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
