package neo4j

object Neo4jOps {

  def initializeDummyPersons(implicit neo4jConnector: Neo4jConnector): Boolean = {
    val NUM = 1000

    val createNodes =
      s"""WITH range(1, $NUM) as ran
         |UNWIND ran AS x
         |CREATE (:Person {id: x, name: 'name ' + x, age: toInteger(rand() * 100)})""".stripMargin

    val createRels =
      s"""WITH range(1, $NUM) as ran
         |UNWIND ran AS x
         |MATCH (n)
         |WHERE n.id = x
         |MATCH (m)
         |WHERE m.id = toInteger(rand() * $NUM)
         |CREATE (n)-[:KNOWS]->(m)""".stripMargin

    neo4jConnector.withSession { session =>
      val nodes = session.run(createNodes)
        .consume().counters().nodesCreated() > 0

      val rels =
        session.run(createRels)
          .consume().counters().relationshipsCreated() > 0

      nodes && rels
    }
  }

  def clearDatabase(implicit neo4jConnector: Neo4jConnector): Boolean = {
    val query =
      """MATCH (n)
        |OPTIONAL MATCH (n)-[r]-()
        |WITH n,r LIMIT 50000
        |DELETE n,r
        |RETURN count(n) as deletedNodesCount""".stripMargin

    neo4jConnector.withSession(session => {
      var removed = 1
      while (removed > 0) {
        removed = session.run(query).consume().counters().nodesDeleted()
      }
      removed
    }) == 0
  }

}
