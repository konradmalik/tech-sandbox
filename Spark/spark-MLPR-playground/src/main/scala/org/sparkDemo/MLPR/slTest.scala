package org.sparkDemo.MLPR

import scala.io.Source
import scala.reflect.runtime.universe

import org.apache.spark.annotation.Experimental
import org.apache.spark.annotation.InterfaceStability
import org.apache.spark.annotation.Since
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.MultilayerPerceptronRegressor
import org.apache.spark.sql.Dataset
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.feature.MinMaxScaler

object slTest extends App with SparkJob {
  import spark.implicits._

  case class SlDataRow(sl: Double, p: Double, T: Double, phi: Double);

  val seed = 666;

  // read data
  val slRawData = Source.fromInputStream(getClass.getResourceAsStream("/Dane1.csv")).getLines().toSeq.tail
    .map(line => {
      val splitted = line.split(",");
      SlDataRow(splitted(0).toDouble, splitted(1).toDouble, splitted(2).toDouble, splitted(3).toDouble);
    })

  val slDataset1: Dataset[SlDataRow] = spark.createDataset(slRawData)

  val slDataset = slDataset1.withColumnRenamed("sl", "label")

  // prepare features
  val inputCols = Array("p", "T", "phi")
  val assembledInputsDF = new VectorAssembler()
    .setInputCols(inputCols)
    .setOutputCol("featuresUnscaled")
    .transform(slDataset)

  // scale features
  val scaler = new MinMaxScaler()
    .setInputCol("featuresUnscaled")
    .setOutputCol("features")
    .fit(assembledInputsDF)

  val scaledData = scaler.transform(assembledInputsDF)

  val selectedData = scaledData.select($"label", $"features")

  // split into train/test
  val Array(trainData, testData) = selectedData.randomSplit(Array(0.7, 0.3), seed)

  // ANN Regressor
  val layers = Array[Int](3, 6, 3, 1)
  val annModel = new MultilayerPerceptronRegressor()
    .setSeed(seed)
    .setLayers(layers)
    .setFeaturesCol("features")
    .setLabelCol("label")
    .setStandardizeLabels(true)
    .setMaxIter(1000000000) // default is too low to converge

  // fit model 
  val annFit = annModel.fit(trainData)

  // check values on train data
  val predictedANN = annFit.transform(testData)

  // check
  //predictedANN.show()

  // evaluate
  val evaluatorANN = new RegressionEvaluator()
    .setLabelCol("label")
    .setPredictionCol("prediction")
    .setMetricName("rmse")

  val rmseANN = evaluatorANN.evaluate(predictedANN)

  println("RMSE of ANN is: " + rmseANN);

  spark.stop()
  spark.close()
}
