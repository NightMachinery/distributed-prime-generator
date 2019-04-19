import SharedSpace._
import akka.actor.ActorSystem

object InitDarkness extends App{
  val system = ActorSystem(masterSystemName, config.getConfig("remoteApp").withFallback(config))

  val cellar = system.actorOf(ColdCellar.props(),cellarName)

  val master = system.actorOf(DarkLord.props(),masterName)

}
