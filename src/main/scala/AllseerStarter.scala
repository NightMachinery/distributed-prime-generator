import akka.actor.ActorSystem
import SharedSpace._
import scala.concurrent.duration._

object AllseerStarter extends App {

  import Allseer._

  val system = ActorSystem("AllseerSys", config.getConfig("localApp").withFallback(config))
  val allseer = system.actorOf(Allseer.props(), "allseer")
  allseer ! AbuseMe(30 seconds)
  allseer ! GetLatestPrimes
}
