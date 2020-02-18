package konradmalik.poc.iotakka.actors

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.testkit.{TestKit, TestProbe}
import konradmalik.poc.iotakka.actors.DeviceManager.{DeviceRegistered, RequestTrackDevice}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class DeviceManagerSpec extends TestKit(ActorSystem("deviceManagerSpec")) with FlatSpecLike with Matchers {

  val probe = TestProbe()
  val managerActor: ActorRef = system.actorOf(DeviceManager.props(), "manager")

  "DeviceManager" should "be able to register a device group" in {
    managerActor.tell(RequestTrackDevice("group1", "device1"), probe.ref)
    probe.expectMsg(DeviceRegistered)
    val deviceActor1 = probe.lastSender

    managerActor.tell(DeviceManager.RequestTrackDevice("group2", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor2 = probe.lastSender
    deviceActor1 should not be deviceActor2

    //check that device actors are working
    deviceActor1.tell(Device.RecordTemperature(0, 1.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(0))
    deviceActor2.tell(Device.RecordTemperature(1, 2.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(1))
    deviceActor2.tell(Device.RecordTemperature(2, 3.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(2))
  }
  it should "be able to list active groups" in {
    managerActor.tell(DeviceManager.RequestTrackDevice("group1", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    managerActor.tell(DeviceManager.RequestTrackDevice("group2", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    managerActor.tell(DeviceManager.RequestGroupList(requestId = 0), probe.ref)
    probe.expectMsg(DeviceManager.ReplyGroupList(requestId = 0, Set("group1", "group2")))
  }
  it should "track the death of actors" in {

    val timeout = 500.millis
    val tester1 = system.actorSelection("/user/manager/group-group2").resolveOne(timeout)
    tester1.andThen {
      case Success(group) =>
        probe.watch(group)
        group ! PoisonPill
        probe.expectTerminated(group)
      case Failure(_) =>
        system.log.info("failed, increase timeout or check actor's path")
    }

    probe.awaitAssert({
      managerActor.tell(DeviceManager.RequestGroupList(requestId = 1), probe.ref)
      probe.expectMsg(DeviceManager.ReplyGroupList(requestId = 1, Set("group1")))
    }, timeout*2)
  }
}
