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

import org.scalaml.Predef._
import org.scalaml.util.FormatUtils
import NaiveBayesModel._
import Likelihood._
import FormatUtils._
import org.scalaml.Predef.Context.ToDouble
import org.scalaml.stats.Stats

/**
 * Class that represents a likelihood for each feature for Naive Bayes classifier.
 *
 * The prior consists of a label (index), the mean of the prior of each dimension of the model,
 * the standard deviation of the prior of each dimension of the model and the class likeliHood.
 *
 * The Naive Bayes assume that the dimension of the model are independent, making the log of
 * the prior additive.
 *
 * @tparam T type of features or variable in observations
 * @constructor Create a likelihood for a specific class.
 * @throws IllegalArgumentException if the array of mean and standard deviation of the
 * likelihood is undefined or if the class likelihood (prior) is out of range ]0,1]
 * @param label  Name or label of the class or prior for which the likelihood is computed.
 * @param muSigma Vector of tuples (mean, standard deviation) of the prior observations for
 * the model
 * @param prior  Probability of occurrence for the class specified by the label.
 *
 * @author Patrick Nicolas
 * @since 0.98 March 11, 2014
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 6 "Naive Bayes Models"
 */
@throws(classOf[IllegalArgumentException])
private[scalaml] class Likelihood[@specialized(Double) T: ToDouble](
    val label: Int,
    val muSigma: Vector[DblPair],
    val prior: Double
) {
  check(muSigma, prior)

  /**
   * Compute the log p(C|x of log of the conditional probability of the class given an
   * observation, obs and a probability density distribution
   *
   * The default density probability function is Normal(0, 1)
   * @param obs parameterized observation
   * @throws IllegalArgumentException if the observations are undefined
   * @return log of the conditional probability p(C|x)
   */
  final def score(obs: Array[T]): Double = {
    require(obs.length > 0, "Likelihood.score Undefined observations")

    // Compute the Log of sum of the likelihood and the class prior probability
    // The log likelihood is computed by adding the log of the density for each dimension.
    // Sum {log p(xi|C) }
    (obs, muSigma).zipped
      .map { case (x, (mu, sig)) => (x, mu, sig) }
      ./:(0.0){ case (post, (x, mu, sig)) => {
        val likelihood: Double = Stats.logGauss(mu, sig, implicitly[ToDouble[T]].apply(x))
         // Avoid large value by setting a minimum value for the density probability
        post + Math.log(if (likelihood < MINLOGARG) MINLOGVALUE else likelihood)
      }} + Math.log(prior) // Add the class likelihood p(C)
  }

  /**
   * Display the content of this Likelihood class with associated labels.
   * @param labels Label of variables used to display content
   */
  def toString(labels: Array[String]): String = {
    val _muSigma: Vector[(Double, Double)] =
      muSigma.map { case (mu, sig) => (mu, if (sig > 0.0) sig else -1.0)}

    // Format the tuple muSigma= (mean, standard deviation) and the Prior
    val first = format(_muSigma, "Label\tMeans", "Standard Deviation\n", MEDIUM, labels)
    s"$first ${format(prior, "\nClass likelihood", MEDIUM)}"
  }

  override def toString: String = toString(Array.empty)
}

/**
 * Companion object for the Naive Bayes Likelihood class. The singleton
 * is used to define the constructor apply for the class.
 * @author Patrick Nicolas
 * @since 0.98.1 March 11, 2014
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 5 Naive Bayes Models
 */
private[scalaml] object Likelihood {
  private val MINLOGARG = 1e-32
  private val MINLOGVALUE = -MINLOGARG

  /**
   * Default constructor for he class Likelihood.
   * @param label  Name or label of the class or prior for which the likelihood is computed.
   * @param muSigma Array of tuples (mean, standard deviation) of the prior observations
   * for the model
   * @param prior  Probability of occurrence for the class specified by the label.
   */
  def apply[@specialized(Double) T: ToDouble](
    label: Int,
    muSigma: Vector[DblPair],
    prior: Double
  ): Likelihood[T] = new Likelihood[T](label, muSigma, prior)

  private def check(muSigma: Vector[DblPair], prior: Double): Unit = {
    require(
      muSigma.nonEmpty,
      "Likelihood.check Historical mean and standard deviation is undefined"
    )
    require(
      prior > 0.0 && prior <= 1.0,
      s"Likelihood.check Prior for the NB prior $prior is out of range"
    )
  }
}

// --------------------------------  EOF --------------------------------------------------------------