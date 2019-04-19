import GeneralConstants._
import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import org.roaringbitmap.RoaringBitmap

import scala.collection.mutable

object ColdCellar {
  def props(): Props = Props(new ColdCellar)

  final case object GetCompletionIndex

  final case class ResultCompletionIndex(completionIndex: BigInt)

  final case class GetNewWorks(howMany: Int)

  final case class ResultNewWorks(works: List[BigInt])

}


class ColdCellar extends Actor with ActorLogging {

  import ColdCellar._

  val master: ActorSelection = getMaster(context)

  // The completion index points to the lowest undone job.
  var completionIndex: BigInt = 0

  //Last completed index; This will not survive a crash because it's mostly useless.
  var lastIndex: BigInt = -1

  val storage = mutable.HashMap.empty[BigInt, RoaringBitmap]

  def persistWork(result: IntSet): Unit = {
    //TODO actually persist
    if (!isDone(result.index)) {
      storage.put(result.index, result.primes)
      log.info(s"Job ${result.index} has just been persisted.")
    }
  }

  //TODO loadWork needs to load from disk.
  def loadWork(index: BigInt): Option[RoaringBitmap] = storage.get(index)

  def isDone(index: BigInt): Boolean = {
    //TODO
    storage.contains(index)
  }

  def outPrimes(index: BigInt): Unit = {
    loadWork(index) match {
      case None =>
        log.info("These primes have not yet been calculated.")
      case Some(innerSet) =>
        val offset = 2 + index * blockSize
        log.info(s"Beginning to output primes of index $index and offset $offset:")
        innerSet.forEach { num => log.info((offset + BigInt(num)).toString) }
    }
  }

  def updateLastIndex(index: BigInt): Unit = {
    if (lastIndex < index) {
      lastIndex = index
    }
  }

  override def receive: Receive = {
    case GetCompletionIndex =>
      sender ! ResultCompletionIndex(completionIndex)
    case NicePig.FinishedWork(result) =>
      persistWork(result)
      updateCompletionIndex(result.index + 1)
      updateLastIndex(result.index)
    case GetNewWorks(howMany) =>
      val works = mutable.MutableList.empty[BigInt]
      var i = 0
      var ci = completionIndex
      while (i < howMany) {
        if (!isDone(ci)) {
          i += 1
          works += ci
        }
        ci += 1
      }
      sender() ! ResultNewWorks(works.toList)
    case Allseer.GetPrimes(index) =>
      outPrimes(index)
    case Allseer.GetLatestPrimes =>
      if (lastIndex >= 0)
        outPrimes(lastIndex)
      else
        log.info("lastIndex is less than zero, don't attempt to get latest primes.")
  }

  def updateCompletionIndex(newIndex: BigInt): Unit = {
    if (newIndex <= completionIndex) {
      return
    }
    completionIndex = newIndex
    //TODO Persist new index
    master ! ResultCompletionIndex(completionIndex)
  }
}
