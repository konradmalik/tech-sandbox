package neo4j

import org.apache.spark.sql.SparkSession

object SparkOps {

  private def createBaseBuilder(appName: String): SparkSession.Builder = {
    SparkSession.builder()
      .config("spark.app.name", appName)
      .config("spark.master", "local[*]")
      // kryo
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      // allow to run multiple RDDs in parallel when using async
      .config("spark.scheduler.mode", "FAIR")
  }

  def createSparkSession(appName: String): SparkSession =
    createBaseBuilder(appName).getOrCreate()


  def createSparkSession(appName: String, neo4jUrl: String, user: String, password: String): SparkSession =
    createBaseBuilder(appName)
      .config("spark.neo4j.bolt.url", neo4jUrl)
      .config("spark.neo4j.bolt.user", user)
      .config("spark.neo4j.bolt.password", password)
      .getOrCreate()

}
