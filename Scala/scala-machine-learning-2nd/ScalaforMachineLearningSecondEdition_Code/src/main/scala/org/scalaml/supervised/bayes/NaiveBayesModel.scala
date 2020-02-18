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
package org.scalaml.supervised.bayes

import org.scalaml.core.Design.Model
import NaiveBayesModel._
import org.scalaml.stats.Stats

/**
 * trait that defines the methods for all the Naive Bayes model. The purpose of the model is to
 * classify a new set of observations.
 * @tparam T type of features in each observation
 * @author Patrick Nicolas
 * @since 0.98 March 8, 2014
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 6 "Naive Bayes Models"
 */
private[scalaml] trait NaiveBayesModel[T] extends Model[NaiveBayesModel[T]] {

  def likelihoods: Seq[Likelihood[T]]

  /**
   * Classify a new observation (or vector) using the Multi-nomial Naive Bayes model.
   * @param x new observation
   * @return the class ID the new observations has been classified.
   * @throws IllegalArgumentException if any of the observation is undefined.
   */
  def classify(x: Array[T]): Int
  def toString(labels: Array[String]): String
  def toString: String
}
/**
 * Companion object to the abstract NaiveBayesModel class. This singleton
 * is used to define the signature of the Density function.
 */
private[scalaml] object NaiveBayesModel {
  /**
   * Signature of the log of the density function used in Naive Bayes Model.
   */
  type LogDensity = Array[Double] => Double

  val logGauss: LogDensity = (x: Array[Double]) => Stats.logGauss(x(0), x(1), x(2))
}

// --------------------------------  EOF --------------------------------------------------------------