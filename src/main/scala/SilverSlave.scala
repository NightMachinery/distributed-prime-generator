import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Cancellable, PoisonPill, Props}
import org.roaringbitmap.RoaringBitmap
import GeneralConstants._

import scala.concurrent.duration._

object SilverSlave {
  def props(): Props = Props(new SilverSlave)

  final case object Timeout

}

class SilverSlave extends Actor with ActorLogging {

  import SilverSlave._
  import Protocol11._
  import context.dispatcher //Used for Scheduler implicit execution context.

  val master: ActorSelection = getMaster(context)

  // State variables!
  var pig: Option[ActorRef] = None
  var processedIndex: BigInt = 0
  var targetIndex: BigInt = 0
  var workingSet: RoaringBitmap = new RoaringBitmap()

  //State end!

  var selfAbuse: Cancellable = createSelfAbuse()

  def createSelfAbuse(): Cancellable = createSelfAbuse(10 seconds, WorkRequest)

  def createSelfAbuse(interval: FiniteDuration, msg: Any): Cancellable = context.system.scheduler.schedule(interval, interval, self, msg)

  def isPrime_BF(num: Int): Boolean = {
    if (num == 2)
      return true

    val sqNum = math.sqrt(num)
    var i = 2
    var result = true
    while (i <= sqNum && result) {
      if (num % i == 0) {
        result = false
      }
      i += 1
    }

    result
  }

  def recreateAbuse(): Unit = {
    selfAbuse.cancel()
    selfAbuse = createSelfAbuse()
  }

  def recreateAbuse(interval: FiniteDuration, msg: Any): Unit = {
    selfAbuse.cancel()
    selfAbuse = createSelfAbuse(interval, msg)
  }

  def askPigForFrame(): Unit = {
    recreateAbuse(slaveTimeout, Timeout)
    pig ! GiveFrame(processedIndex)
  }

  def cleanState(): Unit = {
    killMyPig()
    targetIndex = 0
    processedIndex = 0
    workingSet = new RoaringBitmap()
  }

  def returnToInnocence(): Unit = {
    killMyPig()
    recreateAbuse()
    //No need to call clearState since we call it before starting new job.
  }

  def processFrame(frameData: RoaringBitmap): Unit = {
    frameData.forEach { num =>
      
    }
  }

  override def receive: Receive = {
    case m@WorkRequest =>
      if (pig.isEmpty)
        master ! m
    case NicePig.DelegatedWork(index) =>
      selfAbuse.cancel()
      log.info(s"Work $index received.")

      if (index == 0) {

        val innerSet = new RoaringBitmap()
        var counter = 0
        var currentNum: Int = 2

        while (counter < blockSize) {
          if (isPrime_BF(currentNum)) {
            innerSet.add(counter)
          }
          counter += 1
          currentNum += 1
        }
        sender() ! NicePig.FinishedWork(IntSet(index, innerSet))
        recreateAbuse()
      }
      else {
        cleanState()
        pig = Some(sender())
        targetIndex = index
        askPigForFrame()
      }
    case ResultFrame(frame) =>
      frame match {
        case None =>
          log.warning(s"Received empty frame from ${sender()}")
          returnToInnocence()
        case Some(frameData) =>
          processFrame(frameData)
      }
    case Timeout =>
      returnToInnocence()

  }

  def killMyPig(): Unit = {
    pig match {
      case Some(p) =>
        p ! PoisonPill
        pig = None
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    killMyPig()
    super.preRestart(reason, message)
  }
}
