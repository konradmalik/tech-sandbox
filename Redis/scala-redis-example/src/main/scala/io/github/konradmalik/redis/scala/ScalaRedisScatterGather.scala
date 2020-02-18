package io.github.konradmalik.redis.scala

import akka.actor.ActorSystem
import com.redis._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object ScalaRedisScatterGather extends App {

  // connect to redis with pool of clients
  implicit val clientsPool: RedisClientPool = new RedisClientPool("localhost", 6379)

  // set up executors
  val system = ActorSystem("ScatterGatherSystem")

  val timeout = 5 minutes

  def listPush(opsPerClient: Int, key: String) = {
    clientsPool.withClient(_.lpush(key, 1))
  }
  def listPop(opsPerClient: Int, key: String) = {
    clientsPool.withClient(_.lpop(key))
  }

  private[this] def flow[A](noOfRecipients: Int, opsPerClient: Int, keyPrefix: String, fn: (Int, String) => A) = {
    (1 to noOfRecipients).map(i => {
      Future {
        fn(opsPerClient, keyPrefix + 1)
      }
    })
  }

  // scatter across clients and gather them to do a sum
def scatterGatherWithList(opsPerClient: Int)(implicit clients: RedisClientPool) = {
  // scatter
  val futurePushes = flow(100, opsPerClient, "list_", listPush)

  // concurrent combinator
  val allPushes = Future.sequence(futurePushes)

  // sequential combinator
  val allSum = allPushes.flatMap(result => {
    //gather
    val futurePops = flow(100, opsPerClient, "list_",listPop)
    val allPops = Future.sequence(futurePops)
    allPops.map(_.mkString(","))
  })
  Await.result(allSum, timeout)
}

  println(scatterGatherWithList(2))

  // clean db
  clientsPool.withClient(_.flushdb)
}