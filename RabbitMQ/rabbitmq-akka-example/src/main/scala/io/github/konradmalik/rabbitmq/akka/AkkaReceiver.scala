package io.github.konradmalik.rabbitmq.akka

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp.scaladsl.AmqpSource
import akka.stream.alpakka.amqp.{NamedQueueSourceSettings, ReadResult}
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AkkaReceiver extends App with RabbitMQConnected {

  implicit val system: ActorSystem = ActorSystem("AkkaSenderSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val amqpSource: Source[ReadResult, NotUsed] =
    AmqpSource.atMostOnceSource(
      NamedQueueSourceSettings(connectionProvider, queueDeclaration.name)
        .withDeclaration(queueDeclaration)
        .withAckRequired(false),
      bufferSize = 10
    )

  val printingSink: Sink[ReadResult, Future[Done]] = Sink.foreach[ReadResult](rr => println(rr.bytes.utf8String))

  val collectSink: Sink[ReadResult, Future[immutable.Seq[ReadResult]]] = Sink.seq[ReadResult]

  // attach printing sink
  val amqpSourceWithPrinting: Source[ReadResult, NotUsed] = amqpSource.alsoTo(printingSink)

  val result: Future[Seq[ReadResult]] = amqpSourceWithPrinting.take(5).runWith(collectSink)
  result.onComplete(seq => seq.foreach(s => println("collected size is " + s.size)))
}
