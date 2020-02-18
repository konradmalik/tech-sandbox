package org.sparkDemo.MLPR

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession

trait SparkJob {

  //spark
  Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)

  @transient
  implicit lazy val spark: SparkSession = SparkSession
    .builder()
    .master("local[1]")
    .appName(this.getClass.getSimpleName)
    .getOrCreate()
}