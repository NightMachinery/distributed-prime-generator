import akka.actor.{Actor, ActorLogging, Props}
import org.roaringbitmap.RoaringBitmap
import GeneralConstants._

object SilverSlave {
  def props(): Props = Props(new SilverSlave)

}

class SilverSlave extends Actor with ActorLogging {

  import SilverSlave._
  import Protocol11._

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
    case NicePig.DelegatedWork(index) =>
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
  }
}
