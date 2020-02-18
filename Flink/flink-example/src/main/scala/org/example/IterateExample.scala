package org.example

import java.util.Random

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

/**
 * Example illustrating iterations in Flink streaming.
 *
 * The program sums up random numbers and counts additions
 * it performs to reach a specific threshold in an iterative streaming fashion.
 *
 * This example shows how to use:
 *
 *  - streaming iterations,
 *  - buffer timeout to enhance latency,
 *  - directed outputs.
 *
 */
object IterateExample {

  private final val Bound = 100

  def main(args: Array[String]): Unit = {
    // Checking input parameters
    val params = ParameterTool.fromArgs(args)

    // obtain execution environment and set setBufferTimeout to 1 to enable
    // continuous flushing of the output buffers (lowest latency)
    val env = StreamExecutionEnvironment.getExecutionEnvironment.setBufferTimeout(1)

    // make parameters available in the web interface
    env.getConfig.setGlobalJobParameters(params)

    // create input stream of integer pairs
    val inputStream: DataStream[(Int, Int)] = {
      println("Executing Iterate example with default input data set.")
      env.addSource(new RandomFibonacciSource)
    }

    def withinBound(value: (Int, Int)): Boolean = value._1 < Bound && value._2 < Bound

    // create an iterative data stream from the input with 5 second timeout
    val numbers: DataStream[((Int, Int), Int)] = inputStream
      // Map the inputs so that the next Fibonacci numbers can be calculated
      // while preserving the original input tuple
      // A counter is attached to the tuple and incremented in every iteration step
      .map(value => (value._1, value._2, value._1, value._2, 0))
      .iterate(
        (iteration: DataStream[(Int, Int, Int, Int, Int)]) => {
          // calculates the next Fibonacci number and increment the counter
          val step = iteration.map(value =>
            (value._1, value._2, value._4, value._3 + value._4, value._5 + 1))
          // testing which tuple needs to be iterated again
          val feedback = step.filter(value => withinBound(value._3, value._4))
          // giving back the input pair and the counter
          val output: DataStream[((Int, Int), Int)] = step
            .filter(value => !withinBound(value._3, value._4))
            .map(value => ((value._1, value._2), value._5))
          (feedback, output)
        }
        // timeout after 5 seconds
        , 5000L
      )

    println("Printing result to stdout. Use --output to specify output path.")
    numbers.print()

    env.execute("Streaming Iteration Example")
  }

  // *************************************************************************
  // USER FUNCTIONS
  // *************************************************************************

  /**
   * Generate BOUND number of random integer pairs from the range from 0 to BOUND/2
   */
  private class RandomFibonacciSource extends SourceFunction[(Int, Int)] {

    val rnd = new Random()
    var counter = 0
    @volatile var isRunning = true

    override def run(ctx: SourceContext[(Int, Int)]): Unit = {

      while (isRunning && counter < Bound) {
        val first = rnd.nextInt(Bound / 2 - 1) + 1
        val second = rnd.nextInt(Bound / 2 - 1) + 1

        ctx.collect((first, second))
        counter += 1
        Thread.sleep(50L)
      }
    }

    override def cancel(): Unit = isRunning = false
  }

}