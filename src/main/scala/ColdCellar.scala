import GeneralConstants._
import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import org.roaringbitmap.RoaringBitmap

import scala.collection.mutable

object ColdCellar {
  def props(): Props = Props(new ColdCellar)

  final case object GetCompletionIndex

  final case class ResultCompletionIndex(completionIndex: BigInt)

}


class ColdCellar extends Actor with ActorLogging {

  import ColdCellar._

  val masterSelection: ActorSelection = getMaster(context)

  // The completion index points to the lowest undone job.
  var completionIndex: BigInt = 0

  val storage = mutable.HashMap.empty[BigInt, RoaringBitmap]

  def persistWork(result: IntSet): Unit = {
    //TODO actually persist
    storage.put(result.index, result.primes)
  }

  override def receive: Receive = {
    case GetCompletionIndex =>
      sender ! ResultCompletionIndex(completionIndex)
    case NicePig.FinishedWork(result) =>
      persistWork(result)
      updateCompletionIndex(result.index + 1)
  }

  def updateCompletionIndex(newIndex: BigInt): Unit = {
    if (newIndex <= completionIndex) {
      return
    }
    completionIndex = newIndex
    //TODO Persist new index
    masterSelection ! ResultCompletionIndex(completionIndex)
  }
}
