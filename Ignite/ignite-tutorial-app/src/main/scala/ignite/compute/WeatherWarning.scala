package ignite.compute

import org.apache.ignite.binary.BinaryObject
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.lang.IgniteRunnable
import org.apache.ignite.resources.IgniteInstanceResource
import org.apache.ignite.{Ignite, Ignition}

import scala.jdk.CollectionConverters._
import scala.util.Try

object WeatherWarning {

  class ResidentsWarning(cityId: Long)(implicit @IgniteInstanceResource ignite: Ignite) extends IgniteRunnable {

    // BELOW CODE IS EXECUTED ON THE CLUSTER, SO ALL PRINTS ARE ON THE NODE THAT RUNS THE CODE! (may not see those prints locally)
    // try to run as server (setClientMode(false)) to see some of prints
    override def run(): Unit = {
      // Getting an access to Persons cache.
      val people = ignite.cache("SQL_PUBLIC_PERSON").withKeepBinary()

      val query = new ScanQuery[BinaryObject, BinaryObject]();

      lazy val cursor = people.query(query)
      Try {

        cursor.iterator().asScala.foreach { entry =>
          val personKey = entry.getKey
          println(s"personKey: $personKey")
          // Picking Denver residents only only.
          if (personKey.field[Long]("city_id") == cityId) {
            val person = entry.getValue
            // send info or something
            println(s"key: $personKey, value: $person")
          }
        }
      }.recover { case e: Exception => e.printStackTrace() }
      cursor.close()
    }

  }

  def main(args: Array[String]): Unit = {

    // Connecting to the cluster (explicitly set client mode)
    Ignition.setClientMode(true)
    implicit val ignite: Ignite = Ignition.start("example.xml")

    val cityId = 2; // Id for Denver

    // Sending the logic to a cluster node that stores Denver and its residents.
    ignite.compute().affinityRun("SQL_PUBLIC_CITY", cityId, new ResidentsWarning(cityId))

    ignite.close()

  }

}
