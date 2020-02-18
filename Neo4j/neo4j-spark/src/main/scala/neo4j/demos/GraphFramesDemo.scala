package neo4j.demos

import neo4j.{Neo4jConnector, Neo4jOps, SparkOps}
import org.apache.spark.sql.SparkSession
import org.neo4j.spark.Neo4j

object GraphFramesDemo {

  def main(args: Array[String]): Unit = {

    implicit lazy val spark: SparkSession = SparkOps.createSparkSession("Neo4j",
      Neo4jConnector.Config.URL, Neo4jConnector.Config.USERNAME, Neo4jConnector.Config.PASSWORD)
    implicit lazy val neo4jConnector: Neo4jConnector = Neo4jConnector()
    lazy val neo4jSpark: Neo4j = Neo4jConnector.Neo4jSpark

    // initialize
    assert(Neo4jOps.initializeDummyPersons)
    //

    // load via Cypher query
    println(neo4jSpark.cypher("MATCH (n:Person) RETURN id(n) as id SKIP {_skip} LIMIT {_limit}")
      .partitions(4).batch(25).loadDataFrame
      .count)

    val df = neo4jSpark.pattern("Person", Seq("KNOWS"), "Person").partitions(12).batch(100).loadDataFrame
    df.show()
    // TODO loadRelDataFrame

    // clean db
    assert(Neo4jOps.clearDatabase)
    //
  }

}
