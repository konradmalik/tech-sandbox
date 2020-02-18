import sbt._

object Dependencies {
  lazy val spark: Seq[ModuleID] = {
    val version = "2.3.3"
    Seq(
      "org.apache.spark" %% "spark-core" % version,
      "org.apache.spark" %% "spark-sql" % version,
      "org.apache.spark" %% "spark-mllib" % version
    )
  }
  lazy val transmogrifai: Seq[ModuleID] = {
    val version = "0.6.0"
    Seq("com.salesforce.transmogrifai" %% "transmogrifai-core" % version)
  }
}
