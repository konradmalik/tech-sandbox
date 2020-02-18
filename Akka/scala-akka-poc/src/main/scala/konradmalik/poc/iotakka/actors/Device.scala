package konradmalik.poc.iotakka.actors

import akka.actor.{Actor, ActorLogging, Props}
import konradmalik.poc.iotakka.actors.Device.{ReadTemperature, RecordTemperature, RespondTemperature, TemperatureRecorded}

class Device(groupId: String, deviceId: String) extends Actor with ActorLogging {

  var lastTemperatureReading: Option[Double] = None

  override def preStart(): Unit = log.info(s"Device actor $groupId-$deviceId started")

  override def postStop(): Unit = log.info(s"Device actor $groupId-$deviceId stopped")

  override def receive: Receive = {
    case DeviceManager.RequestTrackDevice(`groupId`, `deviceId`) =>
      sender() ! DeviceManager.DeviceRegistered

    case DeviceManager.RequestTrackDevice(otherGroupId, otherDeviceId) =>
      log.warning(s"Ignoring TrackDevice request for $otherGroupId-$otherDeviceId." +
        s" This actor is responsible for ${this.groupId}-${this.deviceId}")

    case RecordTemperature(id, value) =>
      log.info(s"Recorded temperature reading $value with $id")
      lastTemperatureReading = Some(value)
      sender() ! TemperatureRecorded(id)

    case ReadTemperature(id) => sender() ! RespondTemperature(id, lastTemperatureReading)
  }
}

object Device {
  def props(groupId: String, deviceId: String) = Props(new Device(groupId, deviceId))

  // read
  final case class ReadTemperature(requestId: Long)

  final case class RespondTemperature(requestId: Long, value: Option[Double])


  // write
  final case class RecordTemperature(requestId: Long, value: Double)

  final case class TemperatureRecorded(requestId: Long)

}
