import java.io.{FileInputStream, FileOutputStream}
import java.math.BigInteger

import SharedSpace._
import Protocol11.{GiveFrame, ResultFrame}
import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import org.roaringbitmap.RoaringBitmap
import ammonite.ops._
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}

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

  val wd: Path = home / 'ColdCellar / s"blockSize $blockSize"
  mkdir(wd)

  val ciPath: Path = wd / 'completionIndex

  val master: ActorSelection = getMaster(context)

  // The completion index points to the lowest undone job.
  var completionIndex: BigInt = 0

  //Last completed index; This will not survive a crash because it's mostly useless.
  var lastIndex: BigInt = -1

  val storage = mutable.HashMap.empty[BigInt, RoaringBitmap]

  val kryo = new Kryo()

  override def preStart(): Unit = {
    kryo.register(classOf[RoaringBitmap], new RoaringSerializer())
    kryo.register(classOf[java.math.BigInteger])
    if (exists(ciPath)) {
      completionIndex = read(ciPath, classOf[BigInteger])
      lastIndex = completionIndex //Yeah:))))
      tellMasterCI()
      // If we cached stuff, we'd have to reload the cache here perhaps? :))
    }
  }

  def read[T](path: Path, classType: Class[T]): T = {
    val inp = new Input(new FileInputStream(path.toIO))
    val res = kryo.readObject(inp, classType)
    inp.close()
    res
  }


  def persistWork(result: IntSet): Unit = {
    if (!isDone(result.index)) {
      //storage.put(result.index, result.primes)
      try {
        atomicWrite(result.primes, getPath(result.index))
        log.info(s"Job ${result.index} has just been persisted.")
        updateCompletionIndex(result.index + 1)
        updateLastIndex(result.index)
      }
      catch {
        case e: Throwable =>
          log.error(s"Could not persist work ${result.index}. Exception: $e")
        // Enough?
      }
    }
  }

  def atomicWrite[T](sth: T, path: Path): Unit = {
    val outPath = Path(path + ".tmp")
    rm ! outPath //idk if this is needed
    val out = new Output(new FileOutputStream(outPath.toIO))
    kryo.writeObject(out, sth)
    out.close()
    //rm ! path
    mv.over(outPath, path)
  }

  //TODO loadWork needs to load from disk.
  def loadWork(index: BigInt): Option[RoaringBitmap] = {
    //    if (index ==1){
    //      log.info("shit")
    //    }
    try {
      Some(kryo.readObject(new Input(new FileInputStream(getPath(index).toIO)), classOf[RoaringBitmap]))
    } catch {
      case e: Throwable =>
        log.warning(s"Could not load work $index. Exception: $e")
        None
    }



    //storage.get(index)
  }

  def getPath(index: BigInt): Path = wd / index.toString()

  def isDone(index: BigInt): Boolean = {
    exists ! getPath(index)
    //    storage.contains(index)
  }

  def outPrimes(index: BigInt): Unit = {
    loadWork(index) match {
      case None =>
        log.info("These primes have not yet been calculated.")
      case Some(innerSet) =>
        val offset = getOffset(index)
        log.info(s"Beginning to output primes of index $index and offset $offset:")
        innerSet.forEach { num => log.info(s"${(offset + BigInt(num)).toString} is prime!") }
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
    case GiveFrame(index) =>
      log.info(s"Frame $index requested by ${sender()}.")
      sender() ! ResultFrame(loadWork(index))
  }

  def tellMasterCI(): Unit = master ! ResultCompletionIndex(completionIndex)

  def updateCompletionIndex(newIndex: BigInt): Unit = {
    if (newIndex <= completionIndex) {
      return
    }
    log.info(s"Attempting to update completionIndex to $newIndex")
    completionIndex = newIndex
    atomicWrite(completionIndex.bigInteger, ciPath)
    tellMasterCI()
  }
}
