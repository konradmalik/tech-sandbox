scalaVersion := "2.12.8"

name := "neo4j-scala"
organization := "io.github.konradmalik"
version := "1.0"

libraryDependencies += "org.neo4j.driver" % "neo4j-java-driver" % "1.7.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0-SNAP10" % Test
// scalacheck to fix an issue with scalatest
// (Test / executeTests) java.lang.NoClassDefFoundError: org/scalacheck/Test$TestCallback
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test