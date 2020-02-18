package konradmalik.poc.iotakka.actors

import akka.actor.{Actor, ActorLogging, Props}

class IoTSupervisor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("IoT Application started")

  override def postStop(): Unit = log.info("IoT Application stopped")

  override def receive: Receive = Actor.emptyBehavior

}

object IoTSupervisor {
  def props() = Props(new IoTSupervisor)
}
