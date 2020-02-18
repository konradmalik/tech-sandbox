package ignite.compute

import java.util

import org.apache.ignite.Ignition
import org.apache.ignite.lang.IgniteCallable

import scala.jdk.CollectionConverters._
import scala.util.Try

object WordCount {

  case class WordLengthCallable(word: String) extends IgniteCallable[Int] {
    override def call(): Int = word.length
  }

  def main(args: Array[String]): Unit = {

    // Connecting to the cluster (explicitly set client mode)
    Ignition.setClientMode(true)
    val ignite = Ignition.start("example.xml")

    Try {
      // Iterate through all the words in the sentence and create Callable jobs.
      // callables are created implicitly (java 8 anon class and single method interface)
      val calls: util.List[WordLengthCallable] = "Count characters using callable"
        .split(" ")
        .map(WordLengthCallable.apply).toList.asJava

      // Execute collection of Callables on the grid.
      val responses = ignite.compute().call(calls).asScala

      // Add up all the results.
      val sum = responses.sum

      println("Total number of characters is '" + sum + "'.");
    }.recover { case (e: Exception) => e.printStackTrace() }

    ignite.close()

  }

}
