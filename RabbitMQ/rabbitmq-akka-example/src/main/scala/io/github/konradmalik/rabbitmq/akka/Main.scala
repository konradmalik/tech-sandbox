package io.github.konradmalik.rabbitmq.akka

import akka.stream.alpakka.amqp.{AmqpCachedConnectionProvider, AmqpUriConnectionProvider}

object Main extends App {
  val RABBITMQ_URI = "amqp://guest:guest@localhost:5672"

  val baseConnectionProvider = AmqpUriConnectionProvider(RABBITMQ_URI)
  val cachedConnectionProvider = AmqpCachedConnectionProvider(baseConnectionProvider)

  cachedConnectionProvider.get.close()
}
