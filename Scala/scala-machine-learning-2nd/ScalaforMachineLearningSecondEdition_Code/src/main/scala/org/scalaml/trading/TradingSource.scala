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
package org.scalaml.trading

import org.apache.log4j.Logger

/**
 *  Enumerator that describes the fields used in the extraction of price related data from
 *  the Yahoo finances historical data. The data is loaded from a CSV file.
 *
 *  @author Patrick Nicolas
 *  @since 0.98 Feb 17, 2014
 *  @version 0.99.2
 *  @see Scala for Machine Learning Appendix "Financials 101"
 */
object YahooFinancials extends Enumeration {
  type YahooFinancials = Value
  val DATE, OPEN, HIGH, LOW, CLOSE, VOLUME, ADJ_CLOSE = Value

  import org.scalaml.workflow.data.DataSource.Fields
  private val logger = Logger.getLogger("YahooFinancials")
  private final val EPS = 1e-6
  /**
   *  Convert an field to a double value
   */
  def toDouble(v: Value): Fields => Double = (s: Fields) => s(v.id).toDouble
  def toArray(vs: Array[Value]): Fields => Array[Double] =
    (s: Fields) => vs.map(v => { s(v.id).toDouble })

  /**
   *  Divide to fields as the ratio of their converted values.
   */
  def divide(v1: Value, v2: Value): Fields => Double =
    (s: Fields) => s(v1.id).toDouble / s(v2.id).toDouble

  def ratio(v1: Value, v2: Value): Fields => Double = (s: Fields) => {
    val den = s(v2.id).toDouble
    if (den < EPS) -1.0
    else s(v1.id).toDouble / den - 1.0
  }

  def plus(v1: Value, v2: Value): Fields => Double =
    (s: Fields) => s(v1.id).toDouble + s(v2.id).toDouble

  def minus(v1: Value, v2: Value): Fields => Double =
    (s: Fields) => s(v1.id).toDouble - s(v2.id).toDouble

  def times(v1: Value, v2: Value): Fields => Double =
    (s: Fields) => s(v1.id).toDouble * s(v2.id).toDouble

  /**
   * Extract value of the ADJ_CLOSE field
   */
  val adjClose = (fs: Fields) => fs(ADJ_CLOSE.id).toDouble

  /**
   * Extract value of the VOLUME field
   */
  val volume = (s: Fields) => s(VOLUME.id).toDouble

  /**
   * Computes the relative volatility as (HIGH -LOW)/LOW
   */
  val volatility = ratio(HIGH, LOW)

  /**
   * Computes the ratio of volatility over volume
   */
  val volatilityVol = (s: Fields) =>
    (s(HIGH.id).toDouble - s(LOW.id).toDouble, s(VOLUME.id).toDouble)

  /**
   * Computes the difference between ADJ_CLOSE and OPEN
   */
  val closeOpen = minus(ADJ_CLOSE, OPEN)

  /**
   * Computes the relative difference between ADJ_CLOSE and OPEN
   */
  val vPrice = ratio(ADJ_CLOSE, OPEN)

  /**
   * Computes the ratio of relative volatility over volume as (1 - LOW/HIGH)/VOLUME
   */
  val volatilityByVol = (s: Fields) =>
    (1.0 - s(LOW.id).toDouble / s(HIGH.id).toDouble) / s(VOLUME.id).toDouble

  val volatilityAndVol = (s: Fields) =>
    (1.0 - s(LOW.id).toDouble / s(HIGH.id).toDouble, s(VOLUME.id).toDouble)
}

/**
 *  Enumerator that describes the fields used in the extraction of price related data
 *  from the Google finances historical data. The data is loaded from a CSV file.
 *
 *  @author Patrick Nicolas
 *  @since 0.98 Feb 19, 2014
 *  @version 0.99.2
 *  @see Scala for Machine Learning Appendix: "Financials 101"
 */
object GoogleFinancials extends Enumeration {
  type GoogleFinancials = Value
  val DATE, OPEN, HIGH, LOW, CLOSE, VOLUME = Value

  import org.scalaml.workflow.data.DataSource.Fields

  /**
   * Extract stock price as session close
   */
  val close = (s: Fields) => s(CLOSE.id).toDouble

  /**
   * Extract stock session volume
   */
  val volume = (s: Fields) => s(VOLUME.id).toDouble

  /**
   * Extract volatility of the stock within a session (HIGH - LOW)
   */
  val volatility = (s: Fields) => s(HIGH.id).toDouble - s(LOW.id).toDouble

  /**
   * Extract stock volatility relative to session volume as (HIGH - LOW)/VOLUME
   */
  val volatilityVol = (s: Fields) =>
    (s(HIGH.id).toDouble - s(LOW.id).toDouble, s(VOLUME.id).toDouble)
}

/**
 * Enumerator to extract corporate financial ratio. The Object methods are implemented to
 * load the appropriate field and perform the type conversion
 * @author Patrick Nicolas
 * @since 0.98 May 3, 2014
 * @see Scala for Machine Learning Appendix: Financials 101
 */
object Fundamentals extends Enumeration {
  type Fundamentals = Value
  val TICKER, START_PRICE, END_PRICE, RELATIVE_PRICE_CHANGE, DEBT_TO_EQUITY, DIVIDEND_COVERAGE, OPERATING_MARGIN, SHORT_INTEREST, CASH_PER_SHARE, CASH_PER_SHARE_TO_PRICE, EPS_TREND, DIVIDEND_YIELD, DIVIDEND_TREND = Value

  import org.scalaml.workflow.data.DataSource.Fields
  val ticker = (s: Fields) => s(TICKER.id)
  val relPriceChange = (s: Fields) => s(RELATIVE_PRICE_CHANGE.id).toDouble
  val debtToEquity = (s: Fields) => s(DEBT_TO_EQUITY.id).toDouble
  val dividendCoverage = (s: Fields) => s(DIVIDEND_COVERAGE.id).toDouble
  val shortInterest = (s: Fields) => s(SHORT_INTEREST.id).toDouble
  val cashPerShareToPrice = (s: Fields) => s(CASH_PER_SHARE_TO_PRICE.id).toDouble
  val epsTrend = (s: Fields) => s(EPS_TREND.id).toDouble
  val dividendYield = (s: Fields) => s(DIVIDEND_YIELD.id).toDouble
  val dividendTrend = (s: Fields) => s(DIVIDEND_TREND.id).toDouble
}

// ------------------------------  EOF ---------------------------------------------