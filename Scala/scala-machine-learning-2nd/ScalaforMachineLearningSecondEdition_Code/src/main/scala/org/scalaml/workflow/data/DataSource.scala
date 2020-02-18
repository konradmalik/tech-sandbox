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

import scala.io.Source
import scala.util._
import scala.reflect.ClassTag
import org.apache.log4j.Logger
import org.scalaml.core.ETransform
import org.scalaml.stats.{MinMax, TSeries}
import org.scalaml.Predef._
import DataSource._, TSeries._
import org.scalaml.core.Design.DataSourceConfig

/**
 * Generic class to load or save files into either HDFS or local files system. The
 * data source loads content from
 * {{{
 *   A file if the path name is a csv delimited file
 *   A list of files if the path name is a directory contains csv delimited files
 *   A list of csv delimited files associated to a list of symbol as 'symbol" => "symbol.csv"
 * }}}
 *
 * @constructor Create a data source.
 * @throws IllegalArgumentException if the path name is undefined or the headerLines value
 * is out of range
 * @param config configuration parameters for the data source
 * @param srcFilter Source filter applied to the data source stream.
 *
 * @author Patrick Nicolas
 * @since 0.98 December 11, 2013
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 2 Data Pipelines
 */
final private[scalaml] class DataSource(
    config: DataSourceConfig,
    srcFilter: Option[Fields => Boolean] = None
) extends ETransform[List[Fields => Double], Vector[Array[Double]]](config) {
  check(config)

  private val logger = Logger.getLogger("DataSource")

  /**
   * List of CSV files contained in a directory defined in the path if
   * it is a directory. The list contains the name of the path is
   * it is a file
   */
  lazy val filesList: Try[Array[String]] = Try {
    import java.io.File
    val file = new File(config.pathName)
    if (file.isDirectory)
      file.listFiles.map(_.getName)
    else
      Array[String](config.pathName)
  }

  /**
   * Load and convert the content of a file into a list of fields. The fields
   * are defined as a sequence of type T.
   * @param c implicit conversion of a String to a type.
   * @return List of sequence of fields of the extraction has been successful, None otherwise
   */
  def loadConvert[T: ClassTag](implicit c: String => T): Try[List[Array[T]]] = Try {
    val pathName = getClass.getResource(config.pathName).getPath
    val src = Source.fromFile(pathName)
    val fields = src.getLines().map(_.split(CSV_DELIM).map(c(_))).toList
    src.close()
    fields
  }

  /**
   * Load, extracts, convert and normalize a list of fields using an extractors.
   *  @throws MatchError if the list of extractor Fields => Double is undefined or is empty
   *  @return PartialFunction of list of extractor of type List[Fields => Double] as input and
   *  a corresponding list of floating point value as output
   */
  override def |> : PartialFunction[List[Fields => Double], Try[Vector[Array[Double]]]] = {
    case fields: List[Fields => Double] if fields.nonEmpty => load.map(data => {
      val convert = (f: Fields => Double) => data._2.map(f(_))

      if (config.normalize)
        fields.map(t =>
          new MinMax[Double](convert(t)).normalize(0.0, 1.0).toArray
        ).toVector
      else
        fields.map(convert(_)).toVector
    })
  }

  import scala.collection._
  /**
   * Generate a Time series of single variables by applying a extractor that
   * converts a line in a file to an array of String and to a Double.
   * @param extr Extractor that convert an array of fields(String) to a double
   * @return A Single variable time series
   * @throws IllegalArgumentException if the extractor is not defined.
   */
  def get(extr: Fields => Double): Try[DblVec] = load.map(data => {
    val nData = data._2.map(extr(_)).toVector
    if (config.normalize) new MinMax[Double](nData).normalize(0.0, 1.0) else nData
  })

  /**
   * Extract a vector from a file of relative path, pathName.
   * @return A vector of double floating point values if successful, None otherwise
   */
  def extract: Try[Seq[Double]] = Try {
    val pathName = getClass.getResource(config.pathName).getPath
    Source.fromFile(pathName)
      .getLines()
      .drop(config.headerLines)
      .map(_.toDouble).toSeq
  }

  def load(extr: Fields => Array[Double]): Try[Vector[Array[Double]]] = load.map(data =>
    if (config.normalize)
      normalizeArray(data._2.map(extr(_)).toVector).get
    else
      data._2.map(extr(_)).toVector
  )

  /**
   * Load data from a comma delimited fields text file and generate
   * a tuple of column names and array of text fields
   */
  private def load: Try[(Fields, Array[Fields])] = Try {
    val src = Source.fromFile(config.pathName)
    val rawFields = src.getLines().map(_.split(CSV_DELIM)).toArray.drop(config.headerLines)

    val fields = if (srcFilter.isDefined) rawFields.filter(srcFilter.get) else rawFields
    val results = if (config.reverseOrder) fields.reverse else fields

    val textFields = (fields(0), results)
    src.close()

    textFields
  }
}

/**
 * Companion object for the DataSource class. The singleton is used
 * to define the DataSource constructors and validate their parameters
 *
 * @author Patrick Nicolas
 * @since 0.98 December 11, 2013
 * @version 0.99.2
 * @see Scala for Machine Learning
 */
private[scalaml] object DataSource {
  final val CSV_DELIM: String = ","
  final val REVERSE_ORDER: Boolean = true
  type Fields = Array[String]

  type U = List[Fields => Double]
  type V = Vector[Array[Double]]
  type PFSRC = PartialFunction[U, Try[V]]

  /**
   * Generate a list of CSV files within a directory, associated with a list of symbol
   * {{{
   * symbol => directoryName/symbol.csv
   * }}}
   * @param directoryName, name of the directory containing the CSV files
   */
  def listSymbolFiles(directoryName: String): Array[String] = {
    require(
      !directoryName.isEmpty,
      "DataSource.listSymbolFiles Directory name is undefined"
    )

    val directory = new java.io.File(directoryName)
    val filesList = directory.listFiles
    if (filesList.nonEmpty) directory.listFiles.map(_.getName) else Array.empty
  }

  /**
   * Default constructor for the DataSource
   * @param pathName Relative path for the data files.
   * @param normalize Flag to normalize data within the range [0,1].
   * @param reverseOrder Flag to re-order/index the data from the last entry to the first entry.
   * @param headerLines Number of header lines in the file.
   * @param filter Source filter applied to the data source stream.
   */
  def apply(
    pathName: String,
    normalize: Boolean,
    reverseOrder: Boolean,
    headerLines: Int,
    filter: Option[Array[String] => Boolean] = None
  ): Try[DataSource] =
    Try(new DataSource(DataSourceConfig(pathName, normalize, reverseOrder, headerLines), filter))

  /**
   * Constructor for the DataSource without field filtering
   * @param pathName Relative path for the data files.
   * @param normalize Flag to normalize data within the range [0,1].
   * @param reverseOrder Flag to re-order/index the data from the last entry to the first entry.
   * @param headerLines Number of header lines in the file.
   */
  def apply(
    pathName: String,
    normalize: Boolean,
    reverseOrder: Boolean,
    headerLines: Int
  ): Try[DataSource] = apply(pathName, normalize, reverseOrder, headerLines, None)

  /**
   * Constructor for the DataSource without field filtering, headerlines. The extraction
   * of the content does not alter the order of the data rows in the file.
   * @param pathName Relative path for the data files.
   * @param normalize Flag to normalize data within the range [0,1].
   */
  def apply(pathName: String, normalize: Boolean): Try[DataSource] =
    apply(pathName, normalize, REVERSE_ORDER, 1, None)

  /**
   * Constructor for the DataSource without field filtering, headerlines. The extraction
   * of the content does not alter the order of the data rows in the file.
   * @param symName Name of the symbol associated to a file in a directory for which the
   * content is to be extracted
   * @param pathName Relative path for the data files.
   * @param normalize Flag to normalize data within the range [0,1].
   */
  def apply(symName: String, pathName: String, normalize: Boolean, headerLines: Int): Try[DataSource] =
    apply(s"$pathName$symName", normalize, REVERSE_ORDER, headerLines, None)

  private def check(config: DataSourceConfig): Unit = {
    require(
      !config.pathName.isEmpty,
      "DataSource.check Undefined path for data source"
    )
    require(
      config.headerLines >= 0,
      s"DataSource.check Incorrect number of header lines ${config.headerLines} for data source"
    )
  }
}

// ----------------------------------   EOF ----------------------------------------------