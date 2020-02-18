name in ThisBuild := "spark-s3-example"
scalaVersion in ThisBuild := "2.12.10"
version in ThisBuild := "1.0"
organization in ThisBuild := "io.github.konradmalik"

lazy val spark = (dev: Boolean) => {
  val version = "2.4.4"
  if (dev)
    Seq(
      "org.apache.spark" %% "spark-core" % version,
      "org.apache.spark" %% "spark-sql" % version
    )
  else
    Seq(
      "org.apache.spark" %% "spark-core" % version % "provided",
      "org.apache.spark" %% "spark-sql" % version % "provided"
    )
}

lazy val sparkS3 = {
  val awsVersion = "1.11.656"
  Seq(
    // hadoop version must be >= 2.8.x
    "org.apache.hadoop" % "hadoop-aws" % "2.8.2",
    // below aws dependencies are actually not needed...
    //    "com.amazonaws" % "aws-java-sdk-core" % awsVersion,
    //    "com.amazonaws" % "aws-java-sdk" % awsVersion,
    //    "com.amazonaws" % "aws-java-sdk-kms" % awsVersion,
    //    "com.amazonaws" % "aws-java-sdk-s3" % awsVersion
  )
}

libraryDependencies ++= spark(true)
libraryDependencies ++= sparkS3
