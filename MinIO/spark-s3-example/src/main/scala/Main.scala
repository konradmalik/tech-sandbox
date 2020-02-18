import org.apache.spark.sql.SparkSession

import scala.util.Try

object Main extends App {
  val spark = SparkSession.builder().master("local[2]")
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .config("spark.hadoop.fs.s3a.endpoint", "http://127.0.0.1:9000")
    .config("spark.hadoop.fs.s3a.path.style.access", "true")
    .config("spark.hadoop.fs.s3a.access.key", "minio")
    .config("spark.hadoop.fs.s3a.secret.key", "minio123")
    .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
    .config("spark.hadoop.log4j.logger.org.apache.hadoop.fs.s3a", "DEBUG")
    .getOrCreate()

  // ****************************
  // WRITE (specific bucket must exist! create it first and add permissions to read and write)
  // ****************************
  val data = Array(1, 2, 3, 4, 5)
  val distData = spark.sparkContext.parallelize(data)

  Try {
    distData.saveAsTextFile("s3a://spark-test/test-write")
    println("file did not exist, created it")
  }.recover { case e: Exception =>
    println("file did exist, reading it")
    println(spark.sparkContext.textFile("s3a://spark-test/test-write").collect().mkString(","))
  }

  spark.close()
}