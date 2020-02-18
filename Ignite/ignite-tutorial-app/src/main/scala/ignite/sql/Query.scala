package ignite.sql

import java.util.{Iterator => JIterator, List => JList}

import org.apache.ignite.Ignition
import org.apache.ignite.cache.query.{FieldsQueryCursor, SqlFieldsQuery}

object Query {

  def main(args: Array[String]): Unit = {

    // Connecting to the cluster (explicitly set client mode)
    Ignition.setClientMode(true)
    val ignite = Ignition.start("example.xml")

    // Getting a reference to an underlying cache created for City table above.
    val cityCache = ignite.cache("SQL_PUBLIC_CITY")

    // Querying data from the cluster using a distributed JOIN.
    val query = new SqlFieldsQuery(
      "SELECT p.name, c.name " +
        " FROM Person p, City c WHERE p.city_id = c.id"
    )

    val cursor: FieldsQueryCursor[JList[_]] = cityCache.query(query)

    val iterator: JIterator[JList[_]] = cursor.iterator()

    while (iterator.hasNext) {
      val row = iterator.next()
      println(row.get(0) + ", " + row.get(1))
    }

    ignite.close()
  }

}
