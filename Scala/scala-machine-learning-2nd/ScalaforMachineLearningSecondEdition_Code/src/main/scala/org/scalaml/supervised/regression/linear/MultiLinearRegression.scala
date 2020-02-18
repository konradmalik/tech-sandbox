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
package org.scalaml.supervised.regression.linear

import scala.util.Try
import scala.language.implicitConversions
import scala.annotation.implicitNotFound

import org.scalaml.Predef._
import org.scalaml.Predef.Context._
import org.scalaml.core.ITransform
import org.scalaml.libraries.commonsmath.MultiLinearRAdapter
import org.scalaml.supervised.regression.{Regression, RegressionModel}
import Regression._

/**
 * Class that defines a Multivariate linear regression. The computation of the regression
 * coefficients uses the Apache commons Math library. The regression model (regression
 * parameters or weights) are initialized only if the training was successful.
 * The implemantation follows the standard design of supervised learning algorithm:
 * - The classifier implements the '''ITransform''' implicit monadic data transformation
 * - The constructor triggers the training of the classifier, making the model immutable
 * - The classifier implements the '''Monitor''' interface to collect profile information for
 * debugging purpose
 *
 * {{{
 * Ordinary least squares regression:
 *    w' = argmin Sum of squares {y(i)  - f(x(i)|w)}
 *    with regression model f(x|w) = w(0) + w(1).x(1) + ... + w(n).x(n)
 * }}}
 *
 * @tparam T data type of features used in this classifier.
 * @constructor Creates multi-variate linear regression
 * @throws IllegalArgumentException if the input time series or the labeled data are undefined
 *                                  or have different sizes
 * @param xt       Input multi-dimensional time series for which regression is to be computed.
 * @param expected Labeled data for the Multivariate linear regression
 * @author Patrick Nicolas
 * @since 0.98 April 19, 2014
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 9 "Regression and regularization" / Ordinary least
 *      squares regression
 * @see org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
 * @see org.scalaml.core.ITransform
 * @see org.scalaml.util.Monitor
 */
@throws(classOf[IllegalArgumentException])
@implicitNotFound(msg = "MultiLinearRegression Implicit conversion $T to Double undefined")
final private[scalaml] class MultiLinearRegression[@specialized(Double) T: ToDouble](
    xt: Vector[Array[T]],
    expected: DblVec
) extends ITransform[Array[T], Double] with Regression {

  import scala.util.Try
  import MultiLinearRegression._

  check(xt, expected)

  /**
   * Data transformation that predicts the value of a vector input.
   *
   * @throws MatchError if the model is undefined or has an incorrect size
   * @return PartialFunction of feature of type Array[T] as input and the predicted value of
   *         type Double as output
   */
  override def |> : PartialFunction[Array[T], Try[Double]] = {
    case x: Array[T] if isModel && x.length == model.map(_.size - 1).getOrElse(0) => Try(margin(x, model.get))
  }

  override protected def train: RegressionModel = {
    val mlr = new MultiLinearRAdapter
    // Create a sample for the label 'expected'
    mlr.createModel(xt.map(_.map(implicitly[ToDouble[T]].apply(_))), expected)

    // Create a regression model with the weights and the residual sum of squared errors
    RegressionModel(mlr.weights, mlr.rss)
  }
}

/**
 * Companion object that defines the constructor for the class MultiLinearRegression
 * and validate its parameters
 *
 * @author Patrick Nicolas
 * @since 0.98.1 April 19, 2014
 * @version 0.99.1
 * @see Scala for Machine Learning Chapter 6 "Regression and regularization" Ordinary least
 *      squares regression
 */
private[scalaml] object MultiLinearRegression {
  /**
   * Default constructor for the MultiLinearRegression class
   *
   * @param xt Input multi-dimensional time series for which regression is to be computed.
   * @param expected  Labeled data for the Multivariate linear regression
   */
  def apply[T: ToDouble](
    xt: Vector[Array[T]],
    expected: DblVec
  ): Try[MultiLinearRegression[T]] = Try(new MultiLinearRegression[T](xt, expected))

  def apply[T: ToDouble](
    xt: Array[Array[T]],
    expected: DblVec
  ): Try[MultiLinearRegression[T]] = MultiLinearRegression[T](xt.toVector, expected)

  // Class parameters validation function
  private def check[T](xt: VSeries[T], y: DblVec): Unit = {
    require(
      xt.nonEmpty,
      "MultiLinearRegression.check Cannot create a regression with undefined time series"
    )
    require(
      y.nonEmpty,
      "MultiLinearRegression.check  Cannot train aregression model with undefined labels"
    )
    require(
      xt.size == y.length,
      s"MultiLinearRegression.check Size of input${xt.size} != size of labels ${y.length}"
    )
  }
}

// ------------------------------  EOF ---------------------------------------