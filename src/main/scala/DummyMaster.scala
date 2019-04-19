import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }

object DummyMaster {
  def props(): Props=Props(new DummyMaster())



}

class DummyMaster extends Actor with ActorLogging {
  import DummyMaster._
  import DummyProtocol._

  override def receive: Receive = {
    case Hello(greeter) =>
      log.info(s"DummyMaster has been greeted by $greeter.")
  }
}
