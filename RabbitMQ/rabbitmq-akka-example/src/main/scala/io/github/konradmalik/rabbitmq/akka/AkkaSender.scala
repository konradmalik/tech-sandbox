package io.github.konradmalik.rabbitmq.akka

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp.AmqpWriteSettings
import akka.stream.alpakka.amqp.scaladsl.AmqpSink
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future


object AkkaSender extends App with RabbitMQConnected {

  implicit val system: ActorSystem = ActorSystem("AkkaSenderSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  // rabbit sink
  val amqpSink: Sink[ByteString, Future[Done]] =
    AmqpSink.simple(
      AmqpWriteSettings(connectionProvider)
        .withRoutingKey(queueDeclaration.name)
        .withDeclaration(queueDeclaration)
    )

  val input = Vector("one", "two", "three", "four", "five")
  val writing: Future[Done] =
    Source(input)
      .map(s => ByteString(s))
      .runWith(amqpSink)

}
