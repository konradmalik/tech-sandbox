package org.sparkDemo.MLPR

import org.apache.spark.ml.regression.MultilayerPerceptronRegressor
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.feature.LabeledPoint

object tests extends App with SparkJob {

  val df = spark.createDataFrame(
    Seq(
      LabeledPoint(30, Vectors.dense(1, 2, 3, 4)),
      LabeledPoint(31, Vectors.dense(4, 3, 2, 1)),
      LabeledPoint(60, Vectors.dense(6, 9, 5, 8)),
      LabeledPoint(62, Vectors.dense(8, 5, 5, 8)),
      LabeledPoint(10, Vectors.dense(1, 2, 3, 4)),
      LabeledPoint(44, Vectors.dense(4, 2, 3, 9)),
      LabeledPoint(12, Vectors.dense(1, 6, 4, 1)),
      LabeledPoint(74, Vectors.dense(3, 7, 1, 2)),
      LabeledPoint(32, Vectors.dense(2, 2, 2, 5)),
      LabeledPoint(27, Vectors.dense(4, 9, 3, 2))
      ))

  val mlpr = new MultilayerPerceptronRegressor()
  .setLayers(Array[Int](4, 5, 5, 1))
  .setStandardizeLabels(true)
  
  val model = mlpr.fit(df)
  
  val results = model.transform(df)
  
  val predictions = results.select("prediction").rdd.map(_.getDouble(0))
  
  results.show();
  
  spark.close()
  spark.stop()
}