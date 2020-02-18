/**
 * Copyright (c) 2013-2017  Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License") you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the
 * concepts and algorithms presented in "Scala for Machine Learning 2nd edition".
 * ISBN: 978-1-783355-874-2 Packt Publishing.
 *
 * Version 0.99.2
 */
package org.scalaml.supervised.svm

// Scala standard library
import scala.util.Try
import scala.language.implicitConversions

// 3rd party libraries
import org.apache.log4j.Logger
// ScalaMl classes
import org.scalaml.core.ITransform
import org.scalaml.libraries.libsvm.SVMAdapter._
import org.scalaml.Predef._
import org.scalaml.Predef.Context._
import org.scalaml.util.LoggingUtils._, Math._

/**
 * Support Vector Algorithm for time series of vector of element with parameterized types
 * The model is created or trained during the instantiation of the class. Therefore a
 * model either is properly trained or does not exists. The class relies on the LIBSVM library:
 * [[http://www.csie.ntu.edu.tw/~cjlin/libsvm/ libsvm]]
 *
 * The implementation follows the standard design of supervised learning algorithm:
 * - The classifier implements the '''ITransform''' implicit monadic data transformation
 * - The constructor triggers the training of the classifier, making the model immutable
 * - The classifier implements the '''Monitor''' interface to collect profile information for
 * debugging purpose
 *
 * @tparam T type of elements or features in the time series.
 * @constructor Create a SVM algorithm for a labeled time series given a configuration.
 * @throws IllegalArgumentException if the configuration or the time series is undefined.
 * @param config Configuration of this SVM
 * @param xt Time series to regress or classify
 * @param expected Labeled values for the time series used in the training of the SVM.
 * @author Patrick Nicolas
 * @since 0.98.1 April 28, 2014
 * @version 0.99.2
 * @see org.scalaml.core.ITransform
 * @see org.scalaml.util.Monitor
 * @see Scala for Machine Learning Chapter 8 ''Kernel models and support vector machines'' /
 * Support Vector Classifier
 * @see http://www.csie.ntu.edu.tw/~cjlin/libsvm/
 */
@throws(classOf[IllegalArgumentException])
final class SVM[T: ToDouble](
    config: SVMConfig,
    xt: Vector[Array[T]],
    expected: DblVec
) extends ITransform[Array[T], Double] with Monitor[T] {

  protected val logger = Logger.getLogger("SVM")
  private[this] val c: ToDouble[T] = implicitly[ToDouble[T]]

  // Used to prevent divide by zero
  private[this] val normEPS = config.eps * 1e-7

  // The model of type SVMModel is created during training only
  // if the training succeed during the instantiation of the SVM classifier
  private[this] val model: Option[SVMModel] = train

  /**
   * Access the accuracy of the SVM algorithm.
   * @return accuracy value in the range [0, 1] if the model was successfully trained,
   * None otherwise
   */
  final def accuracy: Option[Double] = model.map(_.accuracy)

  @inline
  final def isModel: Boolean = model.isDefined
  /**
   * Method to compute the Means Square Error for the training of the SVM
   * @return Mean square error as square root of the sum of the square errors, if model was
   * successfully built, None otherwise
   */
  final def mse: Option[Double] = model.map(m => {
    // Simple reducer to compute the sum of the squared error
    val z = (0 until xt.size).map( n => sqr(predictSVM(m, xt(n).map(c(_))) - expected(n)) ).sum

    // The least square error is adjusted by the number of observations.
    Math.sqrt(z) / xt.size
  })

  /**
   * Compute the margin 2/||w|| for the SVM model (distance between the support vectors).
   * The method returns Option[Double.NaN] if the norm of the weights vector is null.
   * @return margin if model was successfully trained, None otherwise of if the model norm is zero
   */
  final def margin: Option[Double] = model.map(m => 1.0/sqrt(m.residuals.reduce( _ * _)))
  import org.scalaml.stats.TSeries._
  /**
   * Data transformation that implements the prediction value using SVM
   * 	@throws MatchError if the model is undefined or has an incorrect size or the input feature
   *  is undefined
   *  @return PartialFunction of feature of type Array[T] as input and the predicted value
   *  as output
   */
  override def |> : PartialFunction[Array[T], Try[Double]] = {
    case x: Array[T] if x.length == dimension(xt) && isModel => Try(predictSVM(model.get, x.map(c(_))))
  }

  /**
   * Compute the accuracy of the model. The computation relies on the cross-validation
   * method if LIBSVM svm class. It returns 0.0 if the cross validation is not selected.
   * The accuracy is computed on the sum of the difference between the actual value and
   * the expected values. A least square method could have been used, too.
   */
  private def accuracy(problem: SVMProblem): Double =
    // IF cross validation is selected
    if (config.isCrossValidation) {
      // Applies the LIBSVM cross-validation
      val target = crossValidateSVM(problem, config.param, config.nFolds, expected.size)

      // compute the sum of the difference of norm between the labels (actual)
      // and the target values (expected or predicted)
      target.view.zip(expected.view)
            .count { case (x, y) => abs(x - y) < config.eps }
            .toDouble / expected.size
    } else 0.0

  /**
   * Training method for SVM that generate a SVMModel
   */
  private def train: Option[SVMModel] = Try {
    val svmProblem = new SVMProblem(xt.size, expected.toArray)
    val dim = dimension(xt)

    // Creates a indexed time series, then
    // initialize the vector of LIBSVM nodes
    xt.indices.foreach( n => svmProblem.update(n, createNode(dim, xt(n).map((c(_))))))
    new SVMModel(trainSVM(svmProblem, config.param), accuracy(svmProblem))
  }._toOption("SVM training failed", logger)

  override def toString: String =
    if (model.isDefined) s"${config.toString}\n${model.get.toString}" else "SVM model undefined"
}

/**
 * Companion object for the SVM algorithms. It is used to defined the constructors for
 * the parameterized class SVM
 * @author Patrick Nicolas
 * @since 0.98.1 April 28, 2014
 * @version 0.99.2
 * @note Scala for Machine Learning Chapter 8 Kernel models and support vector machines.
 */
object SVM {
  /**
   * Default constructor for the support vector machine
   * @param config Configuration of this SVM
   * @param xt Time series to regress or classify
   * @param expected Labeled values for the time series used in the training of the SVM.
   */
  def apply[T: ToDouble](
    config: SVMConfig,
    xt: Vector[Array[T]],
    expected: DblVec
  ): SVM[T] = new SVM[T](config, xt, expected)

  /**
   * Implicit conversion from a SVM[T] to a Try[SVM[T]] type.
   */
  implicit def svm2Try[T: ToDouble](svm: SVM[T]): Try[SVM[T]] = Try(svm)

  private def check[T: ToDouble](
    state: SVMConfig,
    xt: Vector[Array[T]],
    expected: DblVec
  ) {

    require(xt.nonEmpty, "SVM.check  Features for the SVM are undefined")
    require(
      expected.nonEmpty,
      "SVM.check  Labeled observations for the SVM are undefined"
    )
    require(
      xt.size == expected.length,
      s"SVM.check found ${xt.size} observation != ${expected.length} labels required ="
    )
  }
}

// ----------------------------------  EOF ------------------------------------