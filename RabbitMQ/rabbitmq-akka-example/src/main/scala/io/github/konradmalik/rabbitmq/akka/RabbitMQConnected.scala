package io.github.konradmalik.rabbitmq.akka

import akka.stream.alpakka.amqp.{AmqpCachedConnectionProvider, AmqpConnectionProvider, AmqpUriConnectionProvider, QueueDeclaration}

trait RabbitMQConnected {
  private final val RABBITMQ_URI = "amqp://guest:guest@localhost:5672"
  private final lazy val baseConnectionProvider = AmqpUriConnectionProvider(RABBITMQ_URI)
  implicit final lazy val connectionProvider: AmqpConnectionProvider = AmqpCachedConnectionProvider(baseConnectionProvider)

 private final val queueName = "akka-queue"
  implicit final lazy val queueDeclaration: QueueDeclaration = QueueDeclaration(queueName)
}
