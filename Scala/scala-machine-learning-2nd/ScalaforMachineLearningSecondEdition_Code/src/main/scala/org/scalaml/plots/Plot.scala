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
package org.scalaml.plots

import org.jfree.chart.{ChartFrame, JFreeChart}
import org.jfree.chart.title.TextTitle

import org.scalaml.Predef._
import org.scalaml.plots.Plot._
import org.scalaml.util.DisplayUtils

import scala.collection._

private[scalaml] object ChartType extends Enumeration {
  type ChartType = Value
  val LINE, TIME_SERIES, SCATTER, BAR = Value
}

case class Legend(key: String, title: String, xLabel: String, yLabel: String) {
  def toList: List[String] = List[String](key, title, xLabel, yLabel)
}

import java.awt.Stroke

/**
 * Generic plotting class that uses the JFreeChart library.
 * @param legend  Legend for the plot of type '''PlotInfo'''
 * @param theme Configuration for the display of plots of type '''PlotTheme'''
 * @author Patrick Nicolas
 * @since  0.97 November 18, 2013
 * @version 0.99.2
 * @see Scala for Machine Learning Chapter 1 "Getting Started" Writing a simple application /
 * Plotting
 */
abstract private[scalaml] class Plot(legend: Legend, theme: PlotTheme) {

  val strokeList: immutable.List[Stroke]
  def stroke(index: Int) = strokeList(index % strokeList.size)

  /**
   * DisplayUtils array of tuple (x,y) in a 2D plot for a given width and height
   * @param xy Vector of pair (x,y) of double values
   * @param width Width for the display (pixels)
   * @param height Height of the chart (pixels)
   */
  def display(xy: Vector[DblPair], width: Int, height: Int): Boolean

  /**
   * DisplayUtils a vector of Double value in a 2D plot with counts [0, n] on X-Axis and
   * vector value on Y-Axis with a given width and height
   * @param y Array of double values
   * @param width Width for the display (pixels)
   * @param height Height of the chart (pixels)
   */

  def display(y: Array[Double], width: Int, height: Int): Boolean

  protected def createFrame(id: String, chart: JFreeChart): Unit = {
    val frame = new ChartFrame(s"Chart ${count + 1}: $id", chart)
    val anchor = getLocation
    frame.setLocation(anchor._1, anchor._2)
    frame.pack()
    frame.setVisible(true)
  }

  protected def setTitle(title: String, chart: JFreeChart): Unit = {
    val textTitle = new TextTitle(
      title,
      new java.awt.Font("Calibri", java.awt.Font.PLAIN, 20)
    )
    chart.setTitle(textTitle)
  }
}

/**
 * Companion object for the Plot class. This singleton define the method that validate the
 * display of any type of plots used in Scala for Machine Learning.
 * @author Patrick Nicolas
 * @since  November 18, 2013 (0.97)
 * @version 0.98.2
 * @see Scala for Machine Learning Chapter 1 "Getting Started" / Writing a simple application /
 * Plotting
 */
private[scalaml] object Plot {
  import org.scalaml.Predef._

  import scala.collection._

  // type PlotInfo = (String, String, String)
  final val DISPLAY_OFFSET = 25

  var count = 0
  final def getLocation: (Int, Int) = {
    count += 1
    val offset = count * DISPLAY_OFFSET
    (offset, offset % 420)
  }

  private val DEFAULT_WIDTH = 320
  private val DEFAULT_HEIGHT = 240

  private val MIN_DISPLAY_SIZE = 60
  private val MAX_DISPLAY_SIZE = 1280

  /**
   * Validate the input values for the display for a particular plot
   * @tparam T type of the value or collection to be displayed
   * @param y parameterized type value
   * @param height  Height of the display
   * @param width Width of the display
   * @param comment Comments to be added to the chart or plot
   * @throws IllegalArgumentException if the display height or width is out or range or
   * the values are undefined
   */
  def validateDisplay[T](y: T, width: Int, height: Int, comment: String): Boolean = {
    if (DisplayUtils.isChart)
      validateDisplaySize(width, height, comment)
    DisplayUtils.isChart
  }

  /**
   * Validate the input values for the display for a particular plot
   * @param y Array of values to be plotted
   * @param height  Height of the display
   * @param width Width of the display
   * @param comment Comments to be added to the chart or plot
   * @throws IllegalArgumentException if the display height or width is out or range or
   * the values are undefined
   */
  @throws(classOf[IllegalArgumentException])
  def validateDisplayUtils(y: Array[Double], width: Int, height: Int, comment: String): Boolean = {
    if (DisplayUtils.isChart) {
      require(!y.isEmpty, s"$comment Cannot display an undefined series")
      validateDisplaySize(width, height, comment)
    }
    DisplayUtils.isChart
  }

  /**
   * Validate the input values for the display for a particular plot
   * @param y List of values to be plotted
   * @param height  Height of the display
   * @param width Width of the display
   * @param comment Comments to be added to the chart or plot
   * @throws IllegalArgumentException if the display height or width is out or range or
   * the values are undefined
   */
  @throws(classOf[IllegalArgumentException])
  def validateDisplayUtils(
    y: immutable.List[Double],
    width: Int,
    height: Int,
    comment: String
  ): Boolean = {
    validateDisplayUtils(y.toArray, width, height, comment)
  }

  /**
   * Validate the input values for the display for a particular plot
   * @param y vector of values to be plotted
   * @param height  Height of the display
   * @param width Width of the display
   * @param comment Comments to be added to the chart or plot
   * @throws IllegalArgumentException if the display height or width is out or range or
   * the values are undefined
   */
  @throws(classOf[IllegalArgumentException])
  def validateDisplayUtils(
    y: immutable.Vector[Double],
    width: Int,
    height: Int,
    comment: String
  ): Boolean = {
    validateDisplayUtils(y.toArray, width, height, comment)
  }

  /**
   * Validate the display dimension for a particular plot
   * @param height  Height of the display
   * @param width Width of the display
   * @param comment Comments to be added to the chart or plot
   * @throws IllegalArgumentException if the display height or width is out or range
   */
  @throws(classOf[IllegalArgumentException])
  def validateDisplaySize(width: Int, height: Int, comment: String): Boolean = {
    if (DisplayUtils.isChart) {
      require(
        width > MIN_DISPLAY_SIZE && width < MAX_DISPLAY_SIZE,
        s"$comment Width $width is out of range"
      )
      require(
        height > MIN_DISPLAY_SIZE && height < MAX_DISPLAY_SIZE,
        s"$comment  height $height is out of range"
      )
    }
    DisplayUtils.isChart
  }
}

// ------------------------  EOF ----------------------------------------------