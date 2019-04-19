import akka.actor.ActorSystem
import SharedSpace._

object DummyMasterStarter  extends App{

  val system = ActorSystem(masterSystemName, config.getConfig("remoteApp").withFallback(config))
  val master = system.actorOf(DummyMaster.props(),masterName)

}
