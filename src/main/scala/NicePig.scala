
import GeneralConstants._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, PoisonPill, Props}

object NicePig {
  def props(master: ActorRef, worker: ActorRef, index: BigInt): Props = Props(new NicePig(master, worker, index))

  final case object PushWork
  final case class DelegatedWork(index: BigInt)
  final case class FinishedWork(result: IntSet)
}


class NicePig(master: ActorRef, worker: ActorRef, index: BigInt) extends Actor with ActorLogging {
  import NicePig._

  val cellar: ActorSelection = getCellar(context)

  override def receive: Receive = {
    case PushWork =>
      worker ! DelegatedWork(index)
    case fw @ FinishedWork(_) =>
      cellar forward fw
      self ! PoisonPill
  }
}
