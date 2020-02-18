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
package org.scalaml.validation

import org.scalaml.Predef.Context._
import scala.util.Try
import org.scalaml.util.MapUtils.Counter
import org.scalaml.Predef._

/**
 * Sealed case class that implements the labels such as TP (true positive), TN (true negative)
 * FP (false positive) and FN (false negative)
 *
 * @author Patrick Nicolas
 */
private[scalaml] sealed trait VLabel
case class TP() extends VLabel
case class TN() extends VLabel
case class FP() extends VLabel
case class FN() extends VLabel

import BinaryValidation._

/**
 * Immutable class that implements the metrics to validate a model with two classes
 * (or binomial F validation). The validation is applied to a test or validation run
 * The counters for TP, TN, FP and FN are computed during instantiation
 * to the class, Accuracy, precision and recall are computed at run-time (lazy values).
 * {{{
 *    precision = TP/(TP + FP)
 *    recall  TP/(TP + FN)
 *    F1 = 2.precision.recall/(precision + recall)
 *    F2 = 5.precision.recall/(4.precision + recall)
 *    Fn = (1+n.n).precision.recall/(n.n.precision + recall)
 *    G = Sqrt(precision.recall)
 * }}}
 *
 * @constructor Create a class validation instance that compute precision, recall and F1 measure
 * @throws IllegalArgumentException if actualExpected is undefined or has no elements or
 * tpClass is out of range
 * @param expected Array of label or expected values
 * @param xt time series or set of observations used for validating the model
 * @author Patrick Nicolas
 * @since 0.98.2 February 1, 2014
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 2 "Hello World!" / Assessing a model / Validation
 * @see org.scalaml.validation.MultiFValidation
 * @note The quality metrics are computed for the class of index 1 which is contained
 * arbitrary the positive outcome. Computation of precision and recall for an entire model is
 * performed by the class '''MultiFValidation'''
 */
@throws(classOf[IllegalArgumentException])
final private[scalaml] class BinaryValidation[T <: AnyVal](
    expected: Vector[Int],
    xt: Vector[Array[T]]
)(implicit predict: Array[T] => Int) extends AValidation[T](expected, xt) {

  private[this] val counters: Counter[VLabel] =
    expected.zip(xt.map(predict(_)))./:(new Counter[VLabel]) {
      case (cnt, (y, x)) => cnt + classify(x, y)
    }

  /**
   * Implements the computation of the score of the validation of a classifier for
   * the F-measure. By default the score use the F1-score.
   *
   * @return F1 score of the classification
   */
  override def score: Double = f1

  /**
   * Compute the F1 measure for this classifier as (precision + recall)/(2.precision.recall)
   *
   * @return F1 measure using precomputed precision and recall
   */
  lazy val f1: Double = 2.0 * precision * recall / (precision + recall)

  /**
   * Compute the Fn measure for this classifier as
   * (1+n*n).precision.recall/(n*n*precision + recall)
   *
   * @param n degree of the F measure
   * @return Fn measure
   * @throws IllegalArgumentException if the degree of the measure is null or negative
   */
  @throws(classOf[IllegalArgumentException])
  final def fn(n: Int): Double = {
    require(n > 0, s"BinFValidation.fn degree of measure or score $n should be > 0")
    1.0 / (n * n * precision + recall) + 1.0
  }

  /**
   * Compute the geometric mean of precision and recall for this classifier as
   * Sqrt(precision.recall)
   *
   * @return Geometric mean of precision and recall
   */
  final def g: Double = Math.sqrt(precision * recall)

  /**
   * Accuracy of a classifier using TP and TN counters.
   *
   * @return Accuracy for the model
   */
  lazy val accuracy: Double = {
    val num = (counters(TP()) + counters(TN())).toDouble
    num / counters.aggregate(0)((s, kv) => s + kv._2, _ + _)
  }

  /**
   * Precision of a classifier using TP and FP counters.
   *
   * @return Precision for the model if either TP or FP counters is not null
   * @throws IllegalStateException if some counters are null
   */
  @throws(classOf[IllegalStateException])
  lazy val precision = compute(FP())

  /**
   * Recall of a classifier using TP and FN counters.
   *
   * @return recall value for the model or classifier
   * @throws IllegalStateException if some counters are null
   */
  @throws(classOf[IllegalStateException])
  lazy val recall = compute(FN())

  private def compute(vLabel: VLabel): Double = {
    val tpCount = counters(TP())
    if (tpCount < 1)
      throw new IllegalStateException(s"BinFValidation.compute TP = $tpCount should be > 0")
    1.0 / (1.0 + counters(vLabel) / tpCount.toDouble)
  }

  private def classify(computed: Int, label: Int): VLabel =
    if (computed == label)
      if (label == POSITIVE) TP() else TN()
    else if (label == POSITIVE) FN() else FP()
}

/**
 * Companion object to the Class validation class that implement the constructors apply
 *
 * @author Patrick Nicolas
 * @since 0.98 February 1, 2014
 * @version 0.99.2
 * @note Scala for Machine Learning Chapter 2 Hello World! / Assessing a model / Validation
 */
private[scalaml] object BinaryValidation {
  final val POSITIVE = 1
  /**
   * Default constructor for the ClassValidation
   *
   * @param expected Array of pair (actual value, labeled/expected value)
   * @param xt Input time series
   * @param predict Prediction function
   */
  def apply[T <: AnyVal](
    expected: Vector[Int],
    xt: Vector[Array[T]]
  )(predict: Array[T] => Int): Try[BinaryValidation[T]] = Try {
    new BinaryValidation(expected, xt)(predict)
  }

  final def auPRC[T <: AnyVal](binFValidations: List[BinaryValidation[T]]): Double =
    binFValidations./:(0.0)((auprc, binValidation) =>
      auprc + binValidation.precision - binValidation.recall)
}

// --------------------  EOF --------------------------------------------------------