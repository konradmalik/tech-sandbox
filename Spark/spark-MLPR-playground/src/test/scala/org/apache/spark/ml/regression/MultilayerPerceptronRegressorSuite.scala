package org.apache.spark.ml.regression

import org.sparkDemo.MLPR.SparkJob
import org.junit.runner.RunWith
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.feature.LabeledPoint
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MultilayerPerceptronRegressorSuite extends FunSuite with SparkJob {

  test("MLPRegressor behaves reasonably on 1 row of data") {
    val df = spark.createDataFrame(Seq(
      LabeledPoint(30, Vectors.dense(1, 2, 3, 4))))
    val mlpr = new MultilayerPerceptronRegressor().setLayers(Array[Int](4, 1))
    val model = mlpr.fit(df)
    val results = model.transform(df)
    val predictions = results.select("prediction").rdd.map(_.getDouble(0))
    assert(predictions.first() === 30)
  }

  test("MLPRegressor behaves reasonably on 2 rows of data") {
    val df = spark.createDataFrame(Seq(
      LabeledPoint(1, Vectors.dense(1)),
      LabeledPoint(2, Vectors.dense(2))))
          val dftest = spark.createDataFrame(Seq(
      LabeledPoint(1.5, Vectors.dense(1.5))))
    val mlpr = new MultilayerPerceptronRegressor().setLayers(Array[Int](1, 100, 100, 1))
    val model = mlpr.fit(df)
    val results = model.transform(dftest)
    val predictions = results.select("prediction").rdd.map(_.getDouble(0))
    assert(predictions.first() === 1.5)
  }

  test("MLPRegressor behaves reasonably on toy data") {
    val df = spark.createDataFrame(Seq(
      LabeledPoint(30, Vectors.dense(1, 2, 3, 4)),
      LabeledPoint(-15, Vectors.dense(6, 3, 2, 1)),
      LabeledPoint(33, Vectors.dense(2, 2, 3, 4)),
      LabeledPoint(-18, Vectors.dense(6, 4, 2, 1)),
      LabeledPoint(27, Vectors.dense(1, 2, 6, 4)),
      LabeledPoint(-12, Vectors.dense(6, 3, 2, 2))))
    val mlpr = new MultilayerPerceptronRegressor().setLayers(Array[Int](4, 10, 10, 1))
    val model = mlpr.fit(df)
    val results = model.transform(df)
    val predictions = results.select("prediction").rdd.map(_.getDouble(0))
    assert(predictions.max() > 2)
    assert(predictions.min() < -1)
  }

  test("MLPRegressor works with gradient descent") {
    val df = spark.createDataFrame(Seq(
      LabeledPoint(30, Vectors.dense(1, 2, 3, 4)),
      LabeledPoint(-15, Vectors.dense(6, 3, 2, 1)),
      LabeledPoint(33, Vectors.dense(2, 2, 3, 4)),
      LabeledPoint(-18, Vectors.dense(6, 4, 2, 1)),
      LabeledPoint(27, Vectors.dense(1, 2, 6, 4)),
      LabeledPoint(-12, Vectors.dense(6, 3, 2, 2))))
    val layers = Array[Int](4, 5, 8, 1)
    val mlpr = new MultilayerPerceptronRegressor().setLayers(layers).setSolver("gd")
    val model = mlpr.fit(df)
    val results = model.transform(df)
    val predictions = results.select("prediction").rdd.map(_.getDouble(0))
    assert(predictions.max() > 2)
    assert(predictions.min() < -1)
  }

  test("Input Validation") {
    val mlpr = new MultilayerPerceptronRegressor()
    intercept[IllegalArgumentException] {
      mlpr.setLayers(Array[Int]())
    }
    intercept[IllegalArgumentException] {
      mlpr.setLayers(Array[Int](1))
    }
    intercept[IllegalArgumentException] {
      mlpr.setLayers(Array[Int](0, 1))
    }
    intercept[IllegalArgumentException] {
      mlpr.setLayers(Array[Int](1, 0))
    }
    mlpr.setLayers(Array[Int](1, 1))
  }

  test("Test setWeights by training restart") {
    val dataFrame = spark.createDataFrame(Seq(
      LabeledPoint(30, Vectors.dense(1, 2, 3, 4)),
      LabeledPoint(-15, Vectors.dense(6, 3, 2, 1)),
      LabeledPoint(33, Vectors.dense(2, 2, 3, 4)),
      LabeledPoint(-18, Vectors.dense(6, 4, 2, 1)),
      LabeledPoint(27, Vectors.dense(1, 2, 6, 4)),
      LabeledPoint(-12, Vectors.dense(6, 3, 2, 2))))
    val layers = Array[Int](4, 5, 1)
    val trainer = new MultilayerPerceptronRegressor()
      .setLayers(layers)
      .setBlockSize(1)
      .setSeed(12L)
      .setMaxIter(1)
      .setTol(1e-6)
    // Compute weights to initialize network with.
    val initialWeights = trainer.fit(dataFrame).weights
    // Set trainer weights to the initialization for this test.
    trainer.setInitialWeights(initialWeights.copy)
    // Compute new weights with our initialization.
    val weights1 = trainer.fit(dataFrame).weights
    // Reset weights back to our initialization.
    trainer.setInitialWeights(initialWeights.copy)
    // Compute another set of weights with our initialization.
    val weights2 = trainer.fit(dataFrame).weights
    assert(weights1 === weights2,
      "Training should produce the same weights given equal initial weights and number of steps")
  }

}