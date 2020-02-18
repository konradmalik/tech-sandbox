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
package org.scalaml.filtering.dft

// Scala standard library
import org.scalaml.Predef.Context._
import org.scalaml.core.Design.ConfigDouble

import scala.annotation.implicitNotFound
import scala.util.Try

// 3rd party libaries
import org.apache.commons.math3.transform._
import org.apache.log4j.Logger

// ScalaMl classes
import org.scalaml.core.ETransform
import org.scalaml.Predef._

/**
 * Generic Data transformation trait for the Fast Discrete Fourier. The main
 * function of the trait is to pad time series to resize the original data set
 * to the next power to 2. The internal method assumes that the implicit conversion
 * from T to Double is defined.
 * @see org.scala.commons.math3.transform
 * @author Patrick Nicolas
 * @since February 9, 2014 v 0.98.1
 * @version 0.98.2
 * @see Scala for Machine Learning Chapter 3 "Data pre-processing" Discrete Fourier transform
 */
private[scalaml] trait DTransform {

  /**
   * Define the Apache Commons Math configuration parameters for the
   * family of Discrete Fourier transform and series
   */
  object Config {
    final val FORWARD = TransformType.FORWARD
    final val INVERSE = TransformType.INVERSE
    final val SINE = DstNormalization.STANDARD_DST_I
    final val COSINE = DctNormalization.STANDARD_DCT_I
  }

  private[this] def padSize(xtSz: Int, even: Boolean = true): Int = {
    require(xtSz > 0, s"DTransform.padSize Cannot pad a series of size $xtSz")

    // Compute the size-boundary for the padding
    val sz = if (even) xtSz else xtSz - 1
    if ((sz & (sz - 1)) == 0)
      0
    // Compute size of the padding for the DFT
    // by extracting the related bit position
    else {
      var bitPos = 0
      do {
        bitPos += 1
      } while ((sz >> bitPos) > 0)

      // Add 1 slot for  padding to the next power of two
      (if (even) 1 << bitPos else (1 << bitPos) + 1) - xtSz
    }
  }

  /**
   * Pads the input time series with zero values to reach the next boundary of 2 at power of N.
   *
   * The input parameters are validated by the client code.
   * @param vec input time series to filter
   * @param even flag that specifies if the boundary for the data is an even number
   * @return New input array padded for the DFT
   */
  @implicitNotFound(msg = "DTransform.pad Conversion to Double is required")
  protected def pad(vec: DblVec, even: Boolean = true): DblVec = {
    val newSize = padSize(vec.size, even)

    // Fill up the remaining array with 0.0 if the size of the
    // padding exceeds the size of the time series.
    if (newSize > 0) vec ++ Vector.fill(newSize)(0.0) else vec
  }
}

/**
 * Companion object to the class DTransform that define the '''sinc''' and '''sinc2'''
 * functions.
 * @author Patrick Nicolas
 * @since 0.98.1 February 9, 2014
 * @version 0.98.1
 * @see Scala for Machine Learning Chapter 3 "Data pre-processing" / Discrete Fourier transform
 */
object DTransform {

  /**
   * Definition of the generic convolution function used in discrete Fourier transform based
   * low pass filters with f frequency, fC frequency cutoff of this low pass filter and n the power of
   * the convolution
   * @return 1 if frequency is below cutoff, 0 otherwise
   */
  val convol = (n: Int, f: Double, fC: Double) =>
    if (Math.abs(Math.pow(f, n)) < fC) 1.0 else 0.0

  /**
   * Definition of the '''sinc''' convolution function used in discrete Fourier transform based
   * low pass filters
   * @return 1 if frequency is below cutoff, 0 otherwise
   */

  val sinc = convol(1, _: Double, _: Double)

  /**
   * Definition of the '''sinc2''' convolution function used in discrete Fourier transform based
   * low pass filters
   * @return 1 if frequency is below cutoff, 0 otherwise
   */
  val sinc2 = convol(2, _: Double, _: Double)

  val sinc4 = convol(4, _: Double, _: Double)
}

/**
 * Discrete Fourier Transform for time series of element of type T bounded to a Double.
 * This class inherit the generic DTransform to access the padding functions. The transformation
 * uses an explicit configuration, eps and therefore implement '''ETransform'''
 * @tparam T type (view bounded to Double) of the elements of the observations of the
 * time series
 * @constructor Create a Discrete Fourier transform series
 * @param eps maximum value of the first observation to use a fast sine series for the
 * discrete Fourier transform
 * @author Patrick Nicolas
 * @since 0.98  Feb 24, 2014
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 3 "Data pre-processing" Discrete Fourier transform
 */
@implicitNotFound(msg = "DFT Conversion from $T to Double is required")
protected class DFT[T: ToDouble](eps: Double)
    extends ETransform[Vector[T], DblVec](ConfigDouble(eps)) with DTransform {
  private val logger = Logger.getLogger("DFT")

  /**
   * Overload the pipe operator to compute the Discrete Fourier Cosine or Sine transform
   * (time series of frequency values). The type of transform is define at run-time the first
   * value is close to zero (Sine transform) or not (Cosine transform).
   * @throws MatchError if the input time series is undefined or its size is less or equal to 2
   * @return PartialFunction of type vector of Double for input to the Discrete Fourier Transform,
   * and type vector of Double for output as a time series of frequencies..
   */
  override def |> : PartialFunction[Vector[T], Try[DblVec]] = {
    case xv: Vector[T] if xv.size >= 2 => fwrd(xv).map(_._2.toVector)
  }

  /**
   * Extraction of frequencies from a time series using the Discrete Sine and Cosine
   * transforms.
   * @param xv times series input to the Discrete Fourier Transform
   * @return a tuple of Transformer instance and vector of frequencies.
   * @note Exceptions thrown by the Apache Commons Math library
   */
  protected def fwrd(xv: Vector[T]): Try[(RealTransformer, Array[Double])] = {
    import Config._

    // Select which Fourier series should be selected (even as Sine)
    // If the first value is 0.0 (or close) cosine series otherwise
    val rdt = if (Math.abs(implicitly[ToDouble[T]].apply(xv.head)) < eps)
      new FastSineTransformer(SINE)
    else
      new FastCosineTransformer(COSINE)

    // Apply the forward transform to the padded time series
    val padded = pad(xv.map(implicitly[ToDouble[T]].apply(_)), xv.head == 0.0).toArray
    Try((rdt, rdt.transform(padded, FORWARD)))
  }
}

/**
 * Companion object for the Discrete Fourier Cosine and Sine transform.
 * @author Patrick Nicolas
 * @since 0.98.1  February 12, 2014
 * @version 0.98.2
 * @see Scala for Machine Learning Chapter 3 "Data pre-processing"  Discrete Fourier transform
 */
object DFT {

  /**
   * Default constructor for the Discrete Fourier Transform
   */
  def apply[T: ToDouble](eps: Double): DFT[T] = new DFT[T](eps)

  final val DFT_EPS = 1e-8
  def apply[T: ToDouble]: DFT[T] = new DFT[T](DFT_EPS)
}

// -----------------------  EOF --------------------------------------