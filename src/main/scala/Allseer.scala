import GeneralConstants._
import akka.actor.{Actor, ActorLogging, ActorSelection, Props}

object Allseer {

  def props(): Props = Props(new Allseer)

  final case class GetPrimes(index: BigInt)

  final case object GetLatestPrimes

}

class Allseer extends Actor with ActorLogging {
  val master: ActorSelection = getMaster(context)

  override def receive: Receive = {
    case any: Any =>
      master forward any
  }
}
