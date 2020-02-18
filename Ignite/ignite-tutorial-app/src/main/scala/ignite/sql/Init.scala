package ignite.sql

import java.sql.DriverManager

import org.apache.ignite.cache.query.SqlFieldsQuery
import org.apache.ignite.{IgniteCache, Ignition}

import scala.util.Try

object Init {

  def main(args: Array[String]): Unit = {

    // Register JDBC driver.
    Class.forName("org.apache.ignite.IgniteJdbcThinDriver")

    // Open JDBC connection.
    val conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/")

    // Create database tables.
    lazy val stmt = conn.createStatement()
    Try {
      // Create table based on REPLICATED template.
      stmt.executeUpdate(
        "CREATE TABLE City (" +
          " id LONG PRIMARY KEY, name VARCHAR) " +
          " WITH \"template=replicated\""
      )

      // Create table based on PARTITIONED template with one backup.
      stmt.executeUpdate(
        "CREATE TABLE Person (" +
          " id LONG, name VARCHAR, city_id LONG, " +
          " PRIMARY KEY (id, city_id)) " +
          " WITH \"backups=1, affinityKey=city_id\""
      )

      // Create an index on the City table.
      stmt.executeUpdate("CREATE INDEX idx_city_name ON City (name)")

      // Create an index on the Person table.
      stmt.executeUpdate("CREATE INDEX idx_person_name ON Person (name)")
    }.recover { case (e: Exception) => e.printStackTrace() }

    stmt.close()
    conn.close()

    // populate tables
    // Connecting to the cluster (explicitly set client mode)
    Ignition.setClientMode(true)
    val ignite = Ignition.start("example.xml")

    // Getting a reference to an underlying cache created for City table above.
    val cityCache: IgniteCache[Long, AnyRef] = ignite.cache("SQL_PUBLIC_CITY")

    // Getting a reference to an underlying cache created for Person table above.
    val personCache: IgniteCache[AnyRef, AnyRef] =
      ignite.cache("SQL_PUBLIC_PERSON")

    Try {
      // Inserting entries into City.
      val cityQuery = new SqlFieldsQuery(
        "INSERT INTO City (id, name) VALUES (?, ?)"
      );

      cityCache.query(cityQuery.setArgs(1, "Forest Hill")).getAll
      cityCache.query(cityQuery.setArgs(2, "Denver")).getAll
      cityCache.query(cityQuery.setArgs(3, "St. Petersburg")).getAll

      // Inserting entries into Person.
      val personQuery = new SqlFieldsQuery(
        "INSERT INTO Person (id, name, city_id) VALUES (?, ?, ?)"
      );

      personCache.query(personQuery.setArgs(1, "John Doe", 3)).getAll
      personCache.query(personQuery.setArgs(2, "Jane Roe", 2)).getAll
      personCache.query(personQuery.setArgs(3, "Mary Major", 1)).getAll
      personCache.query(personQuery.setArgs(4, "Richard Miles", 2)).getAll
    }.recover { case (e: Exception) => e.printStackTrace() }

    println("queries finished")
    ignite.close()
  }

}
