package org.sparkDemo.MLPR

import scala.io.Source
import scala.reflect.runtime.universe

import org.apache.spark.annotation.Experimental
import org.apache.spark.annotation.InterfaceStability
import org.apache.spark.annotation.Since
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.MinMaxScaler
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.MultilayerPerceptronRegressor
import org.apache.spark.sql.Dataset

object irisTest extends App with SparkJob {
  import spark.implicits._

  // Fisher's Iris Data
  // Sepal length	/ Sepal width	/ Petal length / Petal width / Species
  case class IrisRow(sepallength: Double, sepalwidth: Double, petallength: Double, petalwidth: Double, species: String);

  val seed = 666;

  // read data
  val irisRawData = Source.fromInputStream(getClass.getResourceAsStream("/IRIS.csv")).getLines().toSeq.tail
    .map(line => {
      val splitted = line.split(",").tail;

      IrisRow(splitted(0).toDouble, splitted(1).toDouble, splitted(2).toDouble, splitted(3).toDouble, splitted(4).toString);
    })

  val irisDataset1: Dataset[IrisRow] = spark.createDataset(irisRawData)

  val irisDataset = irisDataset1
    .withColumnRenamed("sepallength", "label")
  //irisDataset1.show()

  // index species strings
  val labeledStrings = new StringIndexer()
    .setInputCol("species")
    .setOutputCol("speciesIndexed")
    .fit(irisDataset)
    .transform(irisDataset)

  // prepare features 
  val inputCols = Array("sepalwidth", "petallength", "petalwidth", "speciesIndexed")
  val assembledInputsDF = new VectorAssembler()
    .setInputCols(inputCols)
    .setOutputCol("featuresUnscaled")
    .transform(labeledStrings)

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
  val layers = Array(4, 50, 1) // Jeremy Nixon layers
  val annModel = new MultilayerPerceptronRegressor()
    .setSeed(seed)
    .setLayers(layers)
    .setFeaturesCol("features")
    .setLabelCol("label")
    .setStandardizeLabels(true)

  // fit model 
  val annFit = annModel.fit(trainData)

  // check values on train data
  val predictedANN = annFit.transform(testData)

  // check
  predictedANN.show()

  // evaluate
  val evaluatorANN = new RegressionEvaluator()
    .setLabelCol("label")
    .setPredictionCol("prediction")
    .setMetricName("rmse")

  val rmseANN = evaluatorANN.evaluate(predictedANN)

  predictedANN.show()
  println("RMSE of ANN is: " + rmseANN);
  // Jeremy Nixon:
  // NN - 0.376
  // DT - 0.451
  // RF - 0.386
  // GBT - 0.444
  // LR - 0.295

  spark.stop();
  spark.close();
}
