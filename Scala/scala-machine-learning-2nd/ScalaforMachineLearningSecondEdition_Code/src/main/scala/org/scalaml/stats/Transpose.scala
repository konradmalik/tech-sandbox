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
package org.scalaml.stats

import scala.reflect.ClassTag
import scala.language.implicitConversions
import org.scalaml.Predef._
/**
 * Singleton that encapsulates the magnet design pattern for the transposition
 * of parameterized multi-dimension time series
 * @author Patrick Nicolas
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 3, "Data Pre-processing" / Time Series
 */
private[scalaml] object Transpose {
  /**
   * Generic "Magnet pattern" interface for Transposition of multi-dimensional time series
   */
  sealed trait Transpose[T] {
    type Result
    def apply(): Result
  }

  /**
   * Transpose a time series fo type VSeries
   * @tparam T type of element (or data point or observation) of the time series
   * @param from Vector of array of elements of type T
   * @return Transposed matrix of type Array of Array
   */
  implicit def xvSeries2MatrixT[T: ClassTag](from: Vector[Array[T]]) = new Transpose[T] {
    type Result = Array[Array[T]]
    def apply(): Result = from.toArray.transpose
  }

  /**
   * Transpose a list of multi-dimensional data of type T
   * @tparam T type of element (or data point or observation) of the time series
   * @param from list of observations of type ''Array[T]''
   * @return Transposed array of array
   */
  implicit def list2Matrix[T: ClassTag](from: List[Array[T]]) = new Transpose[T] {
    type Result = Array[Array[T]]
    def apply(): Result = from.toArray.transpose
  }

  /**
   * Transpose a list of multi-dimensional data of type T
   * @tparam T type of element (or data point or observation) of the time series
   * @param from list of observations of type ''Vector[T]''
   * @return Transposed vector of vectors.
   */
  implicit def list2Vector[T: ClassTag](from: List[Vector[T]]) = new Transpose[T] {
    type Result = Vector[Vector[T]]
    def apply(): Result = from.toVector.transpose
  }

  /**
   * Lifted function for the transposition of multi-dimensional time series
   * @param transposition element of subtype of Transpose
   * @return return type of the constructor on the subtype of Transpose
   */
  def transpose[T: ClassTag](transposition: Transpose[T]): transposition.Result = transposition()
}

// -------------------------------------   EOF -----------------------------------------------------------