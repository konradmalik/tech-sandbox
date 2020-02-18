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
package org.scalaml.libraries.commonsmath

import scala.util.Try

import org.apache.commons.math3.fitting.leastsquares._
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum

/**
 * Class that implements the minimization of the loss function for the logistic
 * regression classifier. It is implemented as the least squares optimization of the
 * Least Square problem defined in Apache Commons Math.
 * @constructor Initialize the optimization function for the logistic regression classifier.
 * @param maxIters Maximum number of iterations allowed during training for the minimization
 * of the loss function.
 * @param maxEvals Maximum number of runs or evaluations allowed during training.
 * @param eps Maximum error allowed during training for the minimization of the loss function.
 * @param lsOptimizer Least squares optimizer used during training.
 * @throws IllegalArgumentException if the maximun number of iterations, maximum number of
 * evaluations or the convergence value are out of bounds, or if the least squares optimizer
 * is undefined.
 *
 * @author Patrick Nicolas
 * @since 0.98.1 May 13, 2014
 * @version 0.99.2
 * @see org.apache.commons.math3.fitting.leastsquares
 * @see org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer
 * @see Scala for Machine Learning Chapter 6 Regression and Regularization/Logistic regression
 */
final private[scalaml] class LogisticRegressionOptimizer(
    val maxIters: Int,
    val maxEvals: Int,
    val eps: Double,
    lsOptimizer: LeastSquaresOptimizer
) {
  import LogisticRegressionOptimizer._

  check(maxIters, maxEvals, eps)

  /**
   * Performs the minimization of the loss function by optimizing the least squares problems
   *
   * @param lsProblem least squares problem as defined in the Apache Commons Math library
   * @return Optimum value for the weights of the logistic regression
   * @see org.apache.commons.math3.fitting.leastsquares
   */
  def optimize(lsProblem: LeastSquaresProblem): Optimum = lsOptimizer.optimize(lsProblem)
}

/**
 * Companion object for the Logistic regression optimizing method. This singleton is
 * used to defined the constructor of the class LogisticRegressionOptimizer and validate
 * its parameters
 * @author Patrick Nicolas
 * @since 0.98.1 May 13, 2014
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 6 Regression and Regularization/Logistic regression
 */
private[scalaml] object LogisticRegressionOptimizer {
  private val EPS_LIMITS = (1e-32, 1.0)
  private val NUM_ITERS_LIMITS = (10, 1000)
  private val NUM_EVALS_LIMITS = (100, 10000)

  private val DEFAULT_NUM_ITERS = 50
  private val DEFAULT_NUM_EVALS = 100
  private val DEFAULT_EPS = 1e-2

  /**
   * Default constructor for the optimizer used in the linear regression
   * @param maxIters Maximum number of iterations allowed during training for the minimization
   * of the loss function.
   * @param maxEvals Maximum number of runs or evaluations allowed during training.
   * @param eps Maximum error allowed during training for the minimization of the loss function.
   * @param lsOptimizer Least squares optimizer used during training.
   */
  def apply(
    maxIters: Int,
    maxEvals: Int,
    eps: Double,
    lsOptimizer: LeastSquaresOptimizer
  ): Try[LogisticRegressionOptimizer] =
    Try(new LogisticRegressionOptimizer(maxIters, maxEvals, eps, lsOptimizer))

  /**
   * Constructor for the optimizer used in the linear regression with a predefined maximum
   * number of iterations, maximum number of evaluations
   * @param eps Maximum error allowed during training for the minimization of the loss function.
   * @param lsOptimizer Least squares optimizer used during training.
   */
  def apply(
    eps: Double,
    lsOptimizer: LeastSquaresOptimizer
  ): Try[LogisticRegressionOptimizer] =
    Try(new LogisticRegressionOptimizer(DEFAULT_NUM_ITERS, DEFAULT_NUM_EVALS, eps, lsOptimizer))

  /**
   * Constructor for the optimizer used in the linear regression with a predefined maximum
   * number of iterations, maximum number of evaluations and convergence criteria
   * @param lsOptimizer Least squares optimizer used during training.
   */
  def apply(lsOptimizer: LeastSquaresOptimizer): Try[LogisticRegressionOptimizer] = Try {
    new LogisticRegressionOptimizer(DEFAULT_NUM_ITERS, DEFAULT_NUM_EVALS, DEFAULT_EPS, lsOptimizer)
  }

  private def check(
    maxIters: Int,
    maxEvals: Int,
    eps: Double
  ): Unit = {

    require(
      maxIters >= NUM_ITERS_LIMITS._1 && maxIters <= NUM_ITERS_LIMITS._2,
      s"Maximum number of iterations $maxIters is out of range"
    )
    require(
      maxEvals >= NUM_EVALS_LIMITS._1 && maxEvals <= NUM_EVALS_LIMITS._2,
      s"Maximum number of evaluations $maxEvals is out of range"
    )
    require(
      maxIters < maxEvals,
      s"Maximum number of iterations $maxIters exceeds maximum number of evaluations $maxEvals"
    )
    require(
      eps >= EPS_LIMITS._1 && eps <= EPS_LIMITS._2,
      s"eps for the optimization of the logistic regression $eps is out of range"
    )
  }
}

//---------------------------------  EOF ---------------------------------------