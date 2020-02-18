package neo4j.demos

import neo4j.{Neo4jConnector, Neo4jOps, SparkOps}
import org.apache.spark.sql.SparkSession
import org.neo4j.spark.Neo4j

object RddDemo {

  def main(args: Array[String]): Unit = {

    implicit lazy val spark: SparkSession = SparkOps.createSparkSession("Neo4j",
      Neo4jConnector.Config.URL, Neo4jConnector.Config.USERNAME, Neo4jConnector.Config.PASSWORD)
    implicit lazy val neo4jConnector: Neo4jConnector = Neo4jConnector()
    lazy val neo4jSpark: Neo4j = Neo4jConnector.Neo4jSpark

    // initialize
    assert(Neo4jOps.initializeDummyPersons)

    val ids = neo4jSpark.cypher("MATCH (p:Person) RETURN p.id AS id").loadRowRdd.cache()
    println(ids.count())
    println(
      ids
        .map(_.getLong(0))
        .reduce(_ + _)
    )

    // inferred schema
    println(ids.first.schema.fieldNames)
    println(ids.first.schema("id"))

    // typed RDD
    println(neo4jSpark.cypher("MATCH (n:Person) RETURN n.id").loadRdd[Long].mean)

    // parametrized RDD
    println(neo4jSpark.cypher("MATCH (n:Person) WHERE n.id <= {maxId} RETURN n.id").param("maxId", 10).loadRowRdd.count)

    // clean db
    assert(Neo4jOps.clearDatabase)
  }

}
