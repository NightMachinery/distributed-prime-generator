import SharedSpace._
import akka.actor.ActorSystem

object SilverSlaveStarter extends App{
  val system = ActorSystem("SilverSys", config.getConfig("localApp").withFallback(config))
  val client = system.actorOf(SilverSlave.props())
}
