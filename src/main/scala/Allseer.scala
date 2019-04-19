import GeneralConstants._
import akka.actor.{Actor, ActorLogging, ActorSelection, Cancellable, Props}

import scala.concurrent.duration._

object Allseer {

  def props(): Props = Props(new Allseer)

  final case class GetPrimes(index: BigInt)

  final case object GetLatestPrimes

  final case class AbuseMe(interval: FiniteDuration)

  final case object CancelAbuse
}

class Allseer extends Actor with ActorLogging {

  import context.dispatcher
  import Allseer._

  var selfAbuse: Option[Cancellable] = None

  def createSelfAbuse(): Cancellable = createSelfAbuse(1 minute)

  def createSelfAbuse(interval: FiniteDuration): Cancellable = context.system.scheduler.schedule(interval, interval, self, GetLatestPrimes)

  val master: ActorSelection = getMaster(context)

  def cancelAbuse(): Unit = {
    selfAbuse match {
      case Some(abuse) =>
        abuse.cancel()
    }
  }


  override def receive: Receive = {
    case AbuseMe(interval) =>
      cancelAbuse()
      selfAbuse = Some(createSelfAbuse(interval))
    case CancelAbuse =>
      cancelAbuse()
    case any: Any =>
      master forward any
  }
}
