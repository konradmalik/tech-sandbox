import sbt._

object Dependencies {
  lazy val ignite = {
    val version = "2.7.6"
    Seq(
      "org.apache.ignite" % "ignite-core" % version,
      "org.apache.ignite" % "ignite-spring" % version,
      "org.apache.ignite" % "ignite-indexing" % version
    )
  }
}

