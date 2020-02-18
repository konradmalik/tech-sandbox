package konradmalik.poc.iotakka

import akka.actor.ActorSystem
import konradmalik.poc.iotakka.actors.IoTSupervisor

import scala.io.StdIn

object IoTApp {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("iot-system")

    try {
      // top level supervisor
      val supervisor = system.actorOf(IoTSupervisor.props(), "iot-supervisor")
      // stop after ENTER
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }
}
