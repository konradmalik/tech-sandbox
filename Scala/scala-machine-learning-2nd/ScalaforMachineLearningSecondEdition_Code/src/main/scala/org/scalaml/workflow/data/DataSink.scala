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
package org.scalaml.workflow.data

import scala.util.{Failure, Success, Try}
import org.apache.log4j.Logger
import org.scalaml.stats.TSeries
import org.scalaml.core.ETransform
import org.scalaml.util.{DisplayUtils, FileUtils, LoggingUtils}
import org.scalaml.Predef.DblVec
import org.scalaml.core.Design.ConfigString

/**
 * Generic class to load or save files into either HDFS or local files system. The
 * persistency of data is defined as a data transformation using an explicit configuration.
 * Therefore '''DataSink''' inherits from &#39;&#39;ETransform&#39;&#39;&#39;
 *
 * @tparam T type of elements of output collections or time series to be stored in file.
 * @constructor Create a DataSink transform associated to a specific path name or database name.
 * @throws IllegalArgumentException if the name of the storage is undefined
 * @param sinkName Name of the storage (file, database, ..).
 * @author Patrick Nicolas
 * @since 0.98 December 15, 2013
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 2 ''Hello World!''
 */
final private[scalaml] class DataSink[T] protected (
    sinkName: String
) extends ETransform[List[Vector[T]], Int](ConfigString(sinkName)) {
  import TSeries._, DataSource._
  import scala.io.Source
  import java.io.File

  require(!sinkName.isEmpty, "DataSink Name of the storage is undefined")

  private val logger = Logger.getLogger("DataSink")

  // Create an handle to a file the results needs to be dump.
  // The directory containing results is created if it does not exist
  private val sinkPath: Option[File] = {
    Try {
      val path = sinkName.substring(0, sinkName.lastIndexOf("/") - 1)
      val dir = new File(path)
      if (!dir.exists)
        dir.mkdir
      dir
    }.toOption
  }

  /**
   * Write the content into the storage with sinkName as identifier.
   * @param content Stringized data to be stored
   * @return true if the content has been successfully stored, false otherwise
   * @throws IllegalArgumentException If the content is not defined.
   */
  def write(content: String): Boolean = {
    require(content.length > 0, "DataSink.write Content undefined")
    assert(sinkPath.isDefined, "DataSink.write Sink path undefined")

    FileUtils.write(content, sinkName, "DataSink")
  }

  /**
   * Write the content of a vector into the storage with sinkName as identifier.
   * @param v vector of type Double to be stored
   * @return true if the vector has been successfully stored, false otherwise
   * @throws IllegalArgumentException If the vector is either undefined or empty.
   */
  @throws(classOf[IllegalArgumentException])
  def write(v: DblVec): Try[Boolean] = Try {
    require(v.nonEmpty, "DataSink.write Cannot persist an undefined vector")
    assert(sinkPath.isDefined, "DataSink.write undefined sink path")

    val content = v.mkString(CSV_DELIM)
    this.write(content.substring(0, content.length - 1))
  }

  /**
   * Persists a set of time series into a predefined storage, sinkFile. The
   * results are written one line per time series
   * @throws MatchError if the list of time series is either undefined or empty
   * @return PartialFunction of a list of parameterized time series as input and the number
   * of time series saved as output (Option)
   */
  @throws(classOf[IllegalArgumentException])
  override def |> : PartialFunction[List[Vector[T]], Try[Int]] = {
    case xs: List[Vector[T]] if xs.nonEmpty && sinkPath.isDefined =>
      import java.io.PrintWriter

      var printWriter: Option[PrintWriter] = None
      Try {
        val content = new StringBuilder
        val numValues = xs.head.size - 1
        val last = xs.size - 1
        // Write into file with one time series per line
        var k = 0
        while (k < numValues) {
          val values = xs.toArray
          Range(0, last) foreach (j => content.append(s"${values(j)(k)},"))
          content.append(s"${values(last)(k)}\n")
          k += 1
        }

        printWriter = Some(new PrintWriter(sinkName))
        printWriter.foreach(_.write(content.toString()))
        k
      } match {
        case Success(k) => Try(k)
        case Failure(e) =>
          DisplayUtils.error("DataSink.|> ", logger)

          if (printWriter.isDefined)
            Try { printWriter.foreach(_.close); 1 }
          else
            Try(DisplayUtils.failure("DataSink.|> printWriter undefined", logger, e))
      }
  }
}

/**
 * Companion object to the class DataSink used to defined its constructor.
 */
object DataSink {
  import scala.annotation.implicitNotFound

  /**
   * Create a DataSink with an implicit conversion of the type parameter to a string.
   * @param sinkPath name of the storage.
   */
  @implicitNotFound("DataSink.apply type Conversion from $T to String undefined")
  def apply[T](sinkPath: String)(implicit f: T => String = (t: T) => t.toString): DataSink[T] = new DataSink[T](sinkPath)
}

// ----------------------------------   EOF ----------------------------------------------