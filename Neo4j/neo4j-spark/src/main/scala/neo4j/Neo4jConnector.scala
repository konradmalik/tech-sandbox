package neo4j

import org.apache.spark.sql.SparkSession
import org.neo4j.driver.v1._
import org.neo4j.spark.Neo4j

import scala.util.{Failure, Success, Try}

class Neo4jConnector(url: String, authToken: AuthToken) {

  private def createDriver = GraphDatabase.driver(url, authToken)

  def withSession[T](operation: Session => T): T = {
    withDriver { driver =>
      val session = driver.session()
      val result = Try(operation(session))
      session.close()
      result match {
        case Success(value) => value
        case Failure(exception) => throw exception
      }
    }
  }

  def withDriver[T](operation: Driver => T): T = {
    val driver = createDriver
    val result = Try(operation(driver))
    driver.closeAsync()
    result match {
      case Success(value) => value
      case Failure(exception) => throw exception
    }
  }

}

object Neo4jConnector {

  def apply(url: String, authToken: AuthToken): Neo4jConnector = new Neo4jConnector(url, authToken)

  def apply() = new Neo4jConnector(Config.URL, AuthTokens.basic(Config.USERNAME, Config.PASSWORD))

  object Config {
    val URL = "bolt://localhost:7687"
    val USERNAME = "NONE"
    val PASSWORD = "NONE"
  }

  def Neo4jSpark(implicit spark: SparkSession) = Neo4j(spark.sparkContext)
}

