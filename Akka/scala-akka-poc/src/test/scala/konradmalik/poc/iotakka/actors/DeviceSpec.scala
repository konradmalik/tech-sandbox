package konradmalik.poc.iotakka.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._

class DeviceSpec extends TestKit(ActorSystem("deviceSpec")) with FlatSpecLike with Matchers {
  val probe = TestProbe()
  val deviceActor: ActorRef = system.actorOf(Device.props("group", "device"))

  "Device actor" should "reply with empty reading if no temperature is known" in {
    deviceActor.tell(Device.ReadTemperature(13), probe.ref)
    val response = probe.expectMsgType[Device.RespondTemperature]
    response.requestId shouldBe 13
    response.value shouldBe None
  }
  it should "reply with latest temperature reading" in {
    deviceActor.tell(Device.RecordTemperature(1, 24), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(1))

    deviceActor.tell(Device.ReadTemperature(2), probe.ref)
    val response1 = probe.expectMsgType[Device.RespondTemperature]
    response1.requestId shouldBe 2
    response1.value shouldBe Some(24)

    deviceActor.tell(Device.RecordTemperature(3, 55), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(3))

    deviceActor.tell(Device.ReadTemperature(4), probe.ref)
    val response2 = probe.expectMsgType[Device.RespondTemperature]
    response2.requestId shouldBe 4
    response2.value shouldBe Some(55)
  }
  it should "reply to registration requests" in {
    deviceActor.tell(DeviceManager.RequestTrackDevice("group", "device"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    probe.lastSender shouldBe deviceActor
  }
  it should "ignore wrong registration requests" in {
    deviceActor.tell(DeviceManager.RequestTrackDevice("wrongGroup", "device"), probe.ref)
    probe.expectNoMessage(500.milliseconds)

    deviceActor.tell(DeviceManager.RequestTrackDevice("group", "wrongDevice"), probe.ref)
    probe.expectNoMessage(500.milliseconds)
  }
}
