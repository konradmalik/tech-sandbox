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
package org.scalaml.scalability.scala

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import scala.collection.parallel.mutable.{ParArray, ParMap}
import scala.collection._
import org.apache.log4j.Logger

import org.scalaml.util.{FormatUtils, DisplayUtils}
import FormatUtils._

/**
 * Generic benchmark for evaluating the performance of Scala parallel collections.
 * @constructor Create a performance benchmark. [times] Number of executions to be performed
 * during the performance testing
 * @throws IllegalArgumentException if the number of executions is out of range
 * @param times Number of executions to be performed during the performance testing
 *
 * @author Patrick Nicolas
 * @since 0.98.1 March 17, 2014
 * @note Scala for Machine Learning Chapter 16 Parallelism in Scala and Akka
 * @version 0.99.2
 */
abstract class Parallelism[U](times: Int) {
  require(times > 0 && times < 512, s"Parallelism number of executions $times is out of range")

  /**
   * Define the map operator for the performance benchmark
   * @param f function invoked by map
   * @param nTasks number of concurrent tasks to implement the operator
   * @return duration of the execution of the parallel collection relative to the
   * non-parallel collection
   */
  def map(f: U => U)(nTasks: Int): Double

  /**
   * Define the filter operator for the performance benchmark
   * @param f function invoked by filter method
   * @param nTasks number of concurrent tasks to implement the operator
   * @return duration of the execution of the parallel collection relative to the
   * non-parallel collection
   */
  def filter(f: U => Boolean)(nTasks: Int): Double

  /**
   * Method to compute the execution time for a higher order Scala method
   * invoking a predefined function g
   * @param g invoked by map or filter during performance test
   * @return Duration of the execution in milliseconds
   */
  protected def timing(g: Int => Unit): Long = {
    // Measure duration of 'times' execution of g
    var startTime = System.currentTimeMillis
    Range(0, times).foreach(g)
    System.currentTimeMillis - startTime
  }
}

/**
 * Class to evaluate the performance of the Scala parallel arrays. The
 * class override the map, reduceLeft methods to collect timing information.
 * @constructor Create a performance benchmark for Scala arrays.
 * @param u  Parameterized array
 * @param v Parameterized parallel array
 * @param times Number of executions in the performance test.
 * @throws IllegalArgumentException if the array of elements is undefined or the number of
 * tasks is out of range
 *
 * @author Patrick Nicolas
 * @since 0.98.1 March 17, 2014
 * @note Scala for Machine Learning Chapter 16 Parallelism in Scala and Akka
 * @version 0.99.2
 */
private[scalaml] class ParallelArray[U](u: Array[U], v: ParArray[U], times: Int) extends Parallelism[U](times) {
  import ParallelArray._, DisplayUtils._

  check(u, v)
  private val logger = Logger.getLogger("ParArrayBenchmark")

  /**
   * Define the map operator for the performance benchmark of the Scala array
   * @param f function invoked by map
   * @param nTasks number of concurrent tasks to implement the operator
   * @return duration of the execution of the parallel Array relative to the non-parallel Array
   */
  override def map(f: U => U)(nTasks: Int): Double = {
    require(
      nTasks > 0 && nTasks < MAX_NUM_TASKS,
      s"ParallelArray.map number of concurrent tasks $nTasks is out of range"
    )

    v.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(nTasks))
    val duration = timing(_ => u.map(f)).toDouble
    val ratio = timing(_ => v.map(f)) / duration

    show(s"$nTasks\t${format(ratio, "", SHORT)}", logger)
    ratio
  }

  /**
   * Define the filter operator for the performance benchmark of Scala arrays
   * @param f function invoked by filter method
   * @param nTasks number of concurrent tasks to implement the operator
   * @return duration of the execution of the parallel Array relative to the non-parallel Array
   */
  override def filter(f: U => Boolean)(nTasks: Int): Double = {
    require(
      nTasks > 0 && nTasks < MAX_NUM_TASKS,
      s"ParallelArray.filter number of concurrent tasks $nTasks is out of range"
    )

    v.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(nTasks))

    val duration = timing(_ => u.filter(f)).toDouble
    val ratio = timing(_ => v.filter(f)) / duration
    show(s"$nTasks\t${format(ratio, "", MEDIUM)}", logger)
    ratio
  }

  /**
   * Implements a reducer operator for the performance benchmark of Scala arrays
   * @param f function invoked by the reducer
   * @param nTasks number of concurrent tasks to implement the operator
   * @return duration of the execution of the parallel Array relative to the non-parallel Array
   */
  def reduce(f: (U, U) => U)(nTasks: Int): Double = {
    require(
      nTasks > 0 && nTasks < MAX_NUM_TASKS,
      s"ParallelArray.filter number of concurrent tasks $nTasks is out of range"
    )

    v.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(nTasks))

    val duration = timing(_ => u.reduceLeft(f)).toDouble
    val ratio = timing(_ => v.reduceLeft(f)) / duration
    show(s"$nTasks\t${format(ratio, "", MEDIUM)}", logger)
    ratio
  }
}

/**
 * Companion object for the class ParArrayBenchmark. This singleton
 * is used to define constant and validate the class parameters.
 * @author Patrick Nicolas
 * @since 0.98.1 March 17, 2014
 * @note Scala for Machine Learning Chapter 16 Parallelism in Scala and Akka
 * @version 0.99.2
 */
private[scalaml] object ParallelArray {
  /**
   * Maximum number of concurrent tasks used in process an array
   */
  val MAX_NUM_TASKS = 64

  protected def check[U](u: scala.Array[U], v: ParArray[U]): Unit = {
    require(
      u.nonEmpty,
      "ParallelArray.check: scala collections undefined"
    )
    require(
      v.nonEmpty,
      "ParallelArray.check: Parallel collections is undefined"
    )
    require(
      u.length == v.size,
      s"ParallelArray: Size of the array ${u.length} is != size of parallel array ${v.size}"
    )
  }
}

/**
 * Class to evaluate the performance of the Scala parallel map. The
 * class override the map, reduceLeft methods to collect timing information
 * @constructor Create a performance benchmark for Scala arrays.
 * @throws IllegalArgumentException if the array of elements is undefined or the number of
 * tasks is out of range
 * @param u  Parameterized map, &#39;&#39;&#39;Map[Int, U]<\b>
 * @param v Parameterized parallel map
 * @param times Number of executions in the performance test.
 * @author Patrick Nicolas
 * @since 0.98.1 March 17, 2014
 * @note Scala for Machine Learning Chapter 16 Parallelism in Scala and Akka
 * @version 0.99.2
 */
final private[scalaml] class ParallelMap[U](
    u: immutable.Map[Int, U],
    v: ParMap[Int, U],
    times: Int
) extends Parallelism[U](times) {
  import ParallelMap._, DisplayUtils._

  check(u, v, times)
  private val logger = Logger.getLogger("ParallelMap")

  /**
   * Define the map operator for the performance benchmark of the Scala map
   * @param f function invoked by map
   * @param nTasks number of concurrent tasks to implement the operator
   * @return duration of the execution of the parallel HashMap relative to the non-parallel HashMap
   */
  override def map(f: U => U)(nTasks: Int): Double = {
    require(
      nTasks > 0 && nTasks < MAX_NUM_TASKS,
      s"ParallelMap.map number of concurrent tasks $nTasks is out of range"
    )

    v.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(nTasks))
    val duration = timing(_ => u.map(e => (e._1, f(e._2)))).toDouble
    val ratio = timing(_ => v.map(e => (e._1, f(e._2)))) / duration
    show(
      s"$nTasks\t${format(ratio, "", MEDIUM)}",
      logger
    )
    ratio
  }

  /**
   * Define the filter operator for the performance benchmark of Scala map
   * @param f function invoked by filter method
   * @param nTasks number of concurrent tasks to implement the operator
   * @return duration of the execution of the parallel HashMap relative to the non-parallel
   * HashMap
   */
  override def filter(f: U => Boolean)(nTasks: Int): Double = {
    require(
      nTasks > 0 && nTasks < MAX_NUM_TASKS,
      s"ParallelMap.filter number of concurrent tasks $nTasks is out of range"
    )

    v.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(nTasks))
    val duration = timing(_ => u.filter(e => f(e._2))).toDouble
    val ratio = timing(_ => v.filter(e => f(e._2))) / duration
    show(s"$nTasks\t${format(ratio, "", MEDIUM)}", logger)
    ratio
  }
}

/**
 * Companion object for the class ParMapBenchmark. This singleton
 * is used to define constant and validate the class parameters.
 * @author Patrick Nicolas
 * @since 0.98.1 March 17, 2014
 * @note Scala for Machine Learning Chapter 16 Parallelism in Scala and Akka
 * @version 0.99.2
 */
private[scalaml] object ParallelMap {

  /**
   * Maximum number of concurrent tasks used in process a parallel map
   */
  private val MAX_NUM_TASKS = 64
  private val MAX_NUM_TIMES = 250

  protected def check[U](u: immutable.Map[Int, U], v: ParMap[Int, U], times: Int): Unit = {
    require(u.nonEmpty, "ParallelMap.check immutable map is undefined ")
    require(v.nonEmpty, "ParallelMap.check Parallel mutable map is undefined")
    require(
      u.size == v.size,
      "ParallelMap.check: Size immutable map ${u.size} != size parallel map ${v.size}"
    )
    require(
      times > 0 && times < MAX_NUM_TIMES,
      s"ParallelMap.check: number of repetition $times is out of range"
    )
  }
}

// ---------------------------------  EOF -------------------------