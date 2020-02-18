package neo4j.demos

import neo4j.{Neo4jConnector, Neo4jOps, SparkOps}
import org.apache.spark.graphx.Graph
import org.apache.spark.graphx.lib.PageRank
import org.apache.spark.sql.SparkSession
import org.neo4j.spark.Neo4j.{NameProp, Pattern}
import org.neo4j.spark._

object GraphXDemo {

  def main(args: Array[String]): Unit = {

    implicit lazy val spark: SparkSession = SparkOps.createSparkSession("Neo4j",
      Neo4jConnector.Config.URL, Neo4jConnector.Config.USERNAME, Neo4jConnector.Config.PASSWORD)
    implicit lazy val neo4jConnector: Neo4jConnector = Neo4jConnector()
    lazy val neo4jSpark: Neo4j = Neo4jConnector.Neo4jSpark

    // initialize
    assert(Neo4jOps.initializeDummyPersons)
    //

    val neoGraph: Graph[Long, Long] = neo4jSpark.pattern(("Person", "id"), ("KNOWS", null), ("Person", "id")).partitions(4).batch(10).loadGraph[Long, Long]

    // What's the size of the graph?
    println(neoGraph.vertices.count)
    println(neoGraph.edges.count)

    // let's run PageRank on this graph
    val graph2 = PageRank.run(neoGraph, 5)

    println(graph2.vertices.sortBy(_._2).take(3))

    // uses pattern from above to save the data, merge parameter is false by default, only update existing nodes
    neo4jSpark.saveGraph(graph2, "rank")
    // uses pattern from parameter to save the data, merge = true also create new nodes and relationships
    neo4jSpark.saveGraph(graph2, "rank", Pattern(NameProp("Person", "id"), Seq(NameProp("FRIEND", "years")), NameProp("Person", "id")), merge = true)

    // clean db
    assert(Neo4jOps.clearDatabase)
    //
  }

}
