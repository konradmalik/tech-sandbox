name := "flink-scala-example"

version := "0.1-SNAPSHOT"

organization := "io.github.konradmalik"

ThisBuild / scalaVersion := "2.12.9"

val flinkVersion = "1.8.1"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-runtime-web" % flinkVersion) // web interface to monitor flink

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies
  )

// make run command include the provided dependencies
Compile / run := Defaults.runTask(Compile / fullClasspath,
  Compile / run / mainClass,
  Compile / run / runner
).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// specify main class for assembly jar
//assembly / mainClass := Some("org.example.Job")
// exclude Scala library from assembly
assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)
