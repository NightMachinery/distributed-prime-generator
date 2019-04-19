import akka.actor.ActorSystem
import SharedSpace._

object DummyClientStarter extends App {
  val system = ActorSystem("ClientSys", config.getConfig("localApp").withFallback(config))
  val client = system.actorOf(DummyClient.props("Monkey7"))

}
