import Dependencies._

scalaVersion in ThisBuild := "2.13.0"
version in ThisBuild := "0.1.0"
organization in ThisBuild := "io.github.konradmalik"

lazy val root = project
  .in(file("."))
  .settings(
    name := "ignite-tutorial-app",
    libraryDependencies ++= ignite
  )
