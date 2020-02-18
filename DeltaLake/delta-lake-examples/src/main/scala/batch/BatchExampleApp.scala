package batch

import java.nio.file.Paths

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit
import org.slf4j.LoggerFactory

object BatchExampleApp extends App {

  val PRODUCTS_DATA_PATH = Paths.get(getClass.getResource("/products.csv").getPath)
  val DELTA_PATH = Paths.get("delta-lake")

  val log = LoggerFactory.getLogger(this.getClass)
  Logger.getLogger("org").setLevel(Level.ERROR)
  //Logger.getLogger("akka").setLevel(Level.OFF)

  val spark = SparkSession.builder()
    .appName(this.getClass.getSimpleName)
    .master("local[*]")
    .getOrCreate()

  //Reading a file
  val productsDf = spark.read.option("header", value = true).csv(PRODUCTS_DATA_PATH.toString)

  //Creating a table
  productsDf.write.format("delta").mode("overwrite").option("overwriteSchema", "true").save(DELTA_PATH.resolve("product").toString)

  //Reading a table
  val productsTable = spark.read.format("delta").load(DELTA_PATH.resolve("product").toString)
  productsTable.show()

  //Adding column to table
  val extProductsDf = productsTable.withColumn("Country", lit("India"))
  extProductsDf.write.format("delta").mode("overwrite").option("mergeSchema", "true").save(DELTA_PATH.resolve("product").toString)

  //New Table
  val extProductsTable = spark.read.format("delta").load(DELTA_PATH.resolve("product").toString)
  extProductsTable.show()

  //Time Travel
  val versionedTable_1 = spark.read.format("delta").option("versionAsOf", 0).load(DELTA_PATH.resolve("product").toString)
  versionedTable_1.show()

  val versionedTable_2 = spark.read.format("delta").option("versionAsOf", 1).load(DELTA_PATH.resolve("product").toString)
  versionedTable_2.show()

  spark.stop()
}
