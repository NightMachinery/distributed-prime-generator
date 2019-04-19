import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, ActorSystem, Props}

import scala.collection.mutable
import GeneralConstants._

object DarkLord {
  def props(): Props = Props(new DarkLord)

  final case object GiveWork

}

class DarkLord extends Actor with ActorLogging {

  import DarkLord._
  import Protocol11._

  val cellar: ActorSelection = getCellar(context)

  var completionIndex: BigInt = 0
  val potentialWorkers: mutable.Queue[ActorRef] = mutable.Queue[ActorRef]()
  val workQueue: mutable.Queue[BigInt] = mutable.Queue[BigInt]()

  def rechargeWorkQueue(): Unit = {
    cellar ! ColdCellar.GetNewWorks(workQueueCapacity - workQueue.length)
  }

  def ensureBigWorkQueue(): Unit = {
    if (workQueue.length < workQueueThreshold) {
      rechargeWorkQueue()
    }
  }

  def pushWork(worker: ActorRef): Unit = {
    if (completionIndex == 0) {
      createPig(0, worker)
    }
    ensureBigWorkQueue()
    if (workQueue.nonEmpty)
      createPig(workQueue.dequeue(), worker)
  }

  def createPig(index: BigInt, worker: ActorRef): Unit = {
    log.info(s"Pig created for work $index, worker ${worker}.")
    val pig = context.system.actorOf(NicePig.props(self, worker, index))
  }

  override def receive: Receive = {
    case WorkRequest =>
      log.info("WorkRequest received.")
      addPotentialWorker(sender())
      self ! GiveWork
    case GiveWork =>
      if (potentialWorkers.nonEmpty) {
        pushWork(potentialWorkers.dequeue())
      }
    case ColdCellar.ResultCompletionIndex(newIndex) =>
      completionIndex = newIndex
    case ColdCellar.ResultNewWorks(works) =>
      for (work <- works) {
        if (!workQueue.contains(work) && workQueue.length < workQueueCapacity) {
          workQueue += work
        }
      }
    case m@(Allseer.GetLatestPrimes | Allseer.GetPrimes) =>
      cellar forward m
  }

  def addPotentialWorker(worker: ActorRef): Unit = {
    if (!potentialWorkers.contains(worker)) {
      potentialWorkers += worker
    }
  }
}
