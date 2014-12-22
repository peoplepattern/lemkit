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
    organization := "io.people8",
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
    initialCommands := "import io.people8._"
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
    .dependsOn(lemkitModel)
    .settings(startScriptForClassesSettings : _*)
}

object Implicits {
  implicit class SuperProject(project: Project) {
    def dependsOnLib(libs: sbt.ModuleID*) = project.settings(libraryDependencies ++= libs)
    def useResolver(rslvrs: sbt.Resolver*) = project.settings(resolvers ++= rslvrs)
  }
}
