package io.github.konradmalik.redis.scala

import com.redis._

object ScalaRedisExample extends App {

  // connect to redis
  val redisClient = new RedisClient("localhost", 6379)

  // set values
  redisClient.set("key", "value of the key")
  println(redisClient.get("key"))

  // list operations
  redisClient.lpush("list1", "b")
  redisClient.rpush("list1", "c")
  redisClient.lpush("list1", "a")
  val list1Len = redisClient.llen("list1")
  println(list1Len)
  println(redisClient.lrange("list1", 0, list1Len.get.toInt - 1))
  redisClient.del("list1")

  // hash maps
  redisClient.hmset("map1", Map("a" -> 1, "b" -> 2))
  redisClient.hmget("map1", "a", "b")

  // clean db
  redisClient.flushdb
}