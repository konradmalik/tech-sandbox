package org.sparkDemo.MLPR

import scala.io.Source
import scala.reflect.runtime.universe

import org.apache.spark.annotation.Experimental
import org.apache.spark.annotation.InterfaceStability
import org.apache.spark.annotation.Since
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.MinMaxScaler
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.MultilayerPerceptronRegressor
import org.apache.spark.sql.Dataset

object housingTest extends App with SparkJob {
  import spark.implicits._

  //    1. CRIM      per capita crime rate by town
  //    2. ZN        proportion of residential land zoned for lots over 
  //                 25,000 sq.ft.
  //    3. INDUS     proportion of non-retail business acres per town
  //    4. CHAS      Charles River dummy variable (= 1 if tract bounds 
  //                 river; 0 otherwise)
  //    5. NOX       nitric oxides concentration (parts per 10 million)
  //    6. RM        average number of rooms per dwelling
  //    7. AGE       proportion of owner-occupied units built prior to 1940
  //    8. DIS       weighted distances to five Boston employment centres
  //    9. RAD       index of accessibility to radial highways
  //    10. TAX      full-value property-tax rate per $10,000
  //    11. PTRATIO  pupil-teacher ratio by town
  //    12. B        1000(Bk - 0.63)^2 where Bk is the proportion of blacks 
  //                 by town
  //    13. LSTAT    % lower status of the population
  //    14. MEDV     Median value of owner-occupied homes in $1000's
  case class HousingRow(crim: Double, zn: Double, indus: Double, chas: Int, nox: Double, rm: Double,
    age: Double, dis: Double, rad: Int, tax: Double, ptratio: Double, b: Double, lstat: Double, medv: Double);

  val seed = 666;

  // read data
  val housingRawData = Source.fromInputStream(getClass.getResourceAsStream("/housing.data")).getLines().toSeq
    .map(line => {
      val splittedTMP = line.split("\\s+")
      val splitted = if (splittedTMP.head.isEmpty()) splittedTMP.tail else splittedTMP;

      HousingRow(splitted(0).toDouble, splitted(1).toDouble, splitted(2).toDouble, splitted(3).toInt, splitted(4).toDouble, splitted(5).toDouble,
        splitted(6).toDouble, splitted(7).toDouble, splitted(8).toInt, splitted(9).toDouble, splitted(10).toDouble, splitted(11).toDouble,
        splitted(12).toDouble, splitted(13).toDouble);
    })

  val housingDataset1: Dataset[HousingRow] = spark.createDataset(housingRawData)

  val housingDataset = housingDataset1
    .withColumnRenamed("MEDV", "label")

  // prepare features
  val inputCols = Array("crim", "zn", "indus", "chas", "nox", "rm",
    "age", "dis", "rad", "tax", "ptratio", "b", "lstat")
  val assembledInputsDF = new VectorAssembler()
    .setInputCols(inputCols)
    .setOutputCol("featuresUnscaled")
    .transform(housingDataset)

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
  val layers = Array[Int](13, 50, 1)
  val annModel = new MultilayerPerceptronRegressor()
    .setSeed(seed)
    .setLayers(layers)
    .setFeaturesCol("features")
    .setLabelCol("label")
    .setMaxIter(100000)
    .setStandardizeLabels(true)

  // fit model 
  val annFit = annModel.fit(trainData)

  // check values on test data
  val predictedANN = annFit.transform(testData)

  // check
  //  predictedANN.show()

  // evaluate
  val evaluatorANN = new RegressionEvaluator()
    .setLabelCol("label")
    .setPredictionCol("prediction")
    .setMetricName("rmse")

  val rmseANN = evaluatorANN.evaluate(predictedANN)

  predictedANN.show()
  println("ANN RMSE is: " + rmseANN);
  // Jeremy Nixon
  // Load Boston
  // NN - 3.87
  // DT - 4.17
  // RF - 3.23
  // GBT - 4.34
  // L2 LR - 4.4

  spark.stop();
  spark.close();
}
