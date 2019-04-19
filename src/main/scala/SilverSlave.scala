import akka.actor.{Actor, ActorLogging, ActorSelection, Cancellable, Props}
import org.roaringbitmap.RoaringBitmap
import GeneralConstants._

import scala.concurrent.duration._

object SilverSlave {
  def props(): Props = Props(new SilverSlave)

}

class SilverSlave extends Actor with ActorLogging {

  import SilverSlave._
  import Protocol11._
  import context.dispatcher //Used for Scheduler implicit execution context.

  val master: ActorSelection = getMaster(context)

  var selfAbuse: Cancellable = createSelfAbuse()

  def createSelfAbuse(): Cancellable = context.system.scheduler.schedule(0.seconds, 10.seconds, self, WorkRequest)

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

  override def receive: Receive = {
    case m@WorkRequest =>
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
      }
      else {
        //TODO
      }

      selfAbuse = createSelfAbuse()
  }
}
