import GeneralConstants._
import Protocol11.GiveFrame
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, PoisonPill, Props, Terminated}
import org.roaringbitmap.RoaringBitmap

object NicePig {
  def props(master: ActorRef, worker: ActorRef, index: BigInt): Props = Props(new NicePig(master, worker, index))

  final case object PushWork

  final case class DelegatedWork(index: BigInt)

  final case class FinishedWork(result: IntSet)


}


class NicePig(master: ActorRef, worker: ActorRef, index: BigInt) extends Actor with ActorLogging {

  import NicePig._
  import context.dispatcher

  val cellar: ActorSelection = getCellar(context)

  override def preStart(): Unit = {
    context.system.scheduler.scheduleOnce(pigTimeout, self, PoisonPill)
    context.watch(worker)
  }

  override def postStop(): Unit = {
    log.info(s"Stopping: Work $index, Pig $self, Worker $worker")
  }

  override def receive: Receive = {
    case PushWork =>
      log.info(s"Pushing: Work $index, Pig $self, Worker $worker")
      worker ! DelegatedWork(index)
    case fw@FinishedWork(_) =>
      cellar forward fw
      log.info(s"Finished Work: Work $index, Pig $self, Worker $worker")
      self ! PoisonPill
    case Terminated(`worker`) =>
      log.info(s"Terminated: Work $index, Pig $self, Worker $worker")
      self ! PoisonPill
    case m@GiveFrame =>
      cellar forward m
  }
}
