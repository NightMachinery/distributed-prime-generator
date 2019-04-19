import akka.actor.ActorSystem
import GeneralConstants._

object DummyClientStarter extends App {
  val system = ActorSystem("ClientSys", config.getConfig("localApp").withFallback(config))
  val client = system.actorOf(DummyClient.props("Monkey7"))

}
