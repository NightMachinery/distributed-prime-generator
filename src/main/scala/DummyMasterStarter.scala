import akka.actor.ActorSystem
import GeneralConstants._

object DummyMasterStarter  extends App{

  val system = ActorSystem(masterSystemName, config.getConfig("remoteApp").withFallback(config))
  val master = system.actorOf(DummyMaster.props(),masterName)

}
