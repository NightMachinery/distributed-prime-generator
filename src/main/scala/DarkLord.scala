import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

import scala.collection.mutable

object DarkLord {
  def props(): Props = Props(new DarkLord)

  final case object GiveWork

}

class DarkLord extends Actor with ActorLogging {

  import DarkLord._
  import Protocol11._

  var completionIndex: BigInt = 0
  val potentialWorkers: mutable.Queue[ActorRef] = mutable.Queue[ActorRef]()

  def pushWork(worker: ActorRef): Unit = {
    if (completionIndex==0){
      val pig = context.system.actorOf(NicePig.props(self,worker,0))
    }

  }

  override def receive: Receive = {
    case WorkRequest =>
      addPotentialWorker(sender())
      self ! GiveWork
    case GiveWork =>
      if (potentialWorkers.nonEmpty) {
        pushWork(potentialWorkers.dequeue())
      }
    case ColdCellar.ResultCompletionIndex(newIndex) =>
      completionIndex = newIndex
  }

  def addPotentialWorker(worker: ActorRef): Unit = {
    if (!potentialWorkers.contains(worker)) {
      potentialWorkers += worker
    }
  }
}
