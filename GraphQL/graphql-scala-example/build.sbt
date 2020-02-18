lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion = "2.6.0-M2"
lazy val sangriaVersion = "1.4.2"
lazy val sangriaSprayVersion = "1.0.1"
lazy val scalaLoggingVersion = "3.9.2"
lazy val logbackVersion = "1.2.3"
lazy val slickVersion = "3.3.0"
lazy val h2Version = "1.4.199"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.github.konradmalik",
      scalaVersion := "2.12.7"
    )),
    name := "graphql-scala-example",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,

      "org.sangria-graphql" %% "sangria" % sangriaVersion,
      "org.sangria-graphql" %% "sangria-spray-json" % sangriaSprayVersion,

      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.h2database" % "h2" % h2Version
    )
  )
