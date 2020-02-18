import Dependencies._

scalaVersion in ThisBuild := "2.11.12"
version in ThisBuild := "0.1.0-SNAPSHOT"
organization in ThisBuild := "io.github.konradmalik"

// set String for avro plugin for titanic features to compile
// to generate avro run "sbt avro:generate"
stringType in AvroConfig := "String"

lazy val root = (project in file("."))
  .settings(
    name := "TransmogrifAI examples",
    libraryDependencies ++= spark,
    libraryDependencies ++= transmogrifai
  )

