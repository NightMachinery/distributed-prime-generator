import akka.actor.ActorSystem
import GeneralConstants._

object AllseerStarter extends App {

  import Allseer._

  val system = ActorSystem("AllseerSys", config.getConfig("localApp").withFallback(config))
  val allseer = system.actorOf(Allseer.props(), "allseer")
  allseer ! GetLatestPrimes

}
