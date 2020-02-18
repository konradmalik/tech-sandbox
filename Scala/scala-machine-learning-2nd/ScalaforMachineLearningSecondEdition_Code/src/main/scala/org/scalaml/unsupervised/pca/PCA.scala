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
package org.scalaml.unsupervised.pca

import scala.util.Try

import org.apache.log4j.Logger
import org.apache.commons.math3.linear._
import org.apache.commons.math3.stat.correlation.Covariance

import org.scalaml.stats.TSeries
import org.scalaml.Predef._
import org.scalaml.Predef.Context._
import org.scalaml.core.ITransform
import org.scalaml.util.LoggingUtils._
import TSeries._

case class PCAModel(covariance: DblMatrix, eigenvalues: Array[Double])

/**
 * Generic class that implements the Principal Component Analysis technique. The
 * extraction of the principal components (Eigenvectors). The class is parameterized
 * as a view bound to Double. The purpose of the class is to compute the covariance
 * matrix and the eigenvalues (normalized values ordered by decreasing order).
 * The features
 * (or variables) associated with a low eigenvalue are eliminated, reducing the dimension
 * of the model and the complexity of any future supervised learning algorithm.
 * @constructor Instantiate a principal component analysis algorithm as a data transformation
 * @tparam T type of element of the dataset to analyze
 *
 * @author Patrick Nicolas
 * @since 0.98 February 26, 2014
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 4 "Unsupervised learning"  Principal Components
 * Analysis
 * @see org.apache.commons.math3.stat.correlation._
 * @see org.apache.commons.math3.linear._
 * @see http://commons.apache.org/proper/commons-math/
 */
@throws(classOf[IllegalArgumentException])
final private[scalaml] class PCA[T: ToDouble](xt: Vector[Array[T]])
    extends ITransform[Array[T], Double] with Monitor[T] {

  require(xt.size > 0, "PCA has undefined input data")

  protected val logger = Logger.getLogger("PCA")
  private[this] val model: Option[PCAModel] = train

  private def train: Option[PCAModel] = zScores(xt).map(x => {
    import org.scalaml.libraries.commonsmath.CommonsMath._
    // Forces a conversion
    val obs: DblMatrix = x.toArray
    // Compute the covariance matrix related to the observations in the time series (3)
    val cov = new Covariance(obs).getCovarianceMatrix

    // Create a Eigenvalue and Eigenvectors decomposition (4)
    val transform = new EigenDecomposition(cov)

    // Retrieve the principal components (or direction)
    val eigenVectors = transform.getV

    // Retrieve the eigen values
    val eigenValues = new ArrayRealVector(transform.getRealEigenvalues)
    val covariance = obs.multiply(eigenVectors).getData
    // Return the tuple (Covariance matrix, Eigenvalues)
    PCAModel(covariance, eigenValues.toArray)
  }).toOption

  /**
   * Data transformation that implements the extraction of the principal components
   * from a time series. The methods uses the Apache Commons Math library to compute
   * eigenvectors and eigenvalues. All the exceptions thrown by the Math library during
   * the manipulation of matrices are caught in the method.
   * @throws MatchError if the input time series is undefined or have no elements
   * @return PartialFunction of time series of elements of type T as input to the Principal
   * Component Analysis and tuple Covariance matrix and vector of eigen values as output
   */
  override def |> : PartialFunction[Array[T], Try[Double]] = {
    case x: Array[T] if x.length == dimension(xt) && model.isDefined =>
      Try(margin(x, model.get.eigenvalues))
  }

  override def toString: String =
    model.map(m => {
      val covStr = m.covariance./:(new StringBuilder)((b, r) =>
        b.append(s"${r.mkString(" ")}\n")).toString()

      s"""\nEigenvalues:\n${m.eigenvalues.mkString(" ,")}\n\nCovariance matrix\n
	  | $covStr""".stripMargin
    }).getOrElse("PCA model undefined")
}

//--------------------------------  EOF -------------------------------------------------------------------------