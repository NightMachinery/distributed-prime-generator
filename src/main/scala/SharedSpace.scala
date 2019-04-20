import akka.actor.{ActorContext, ActorSelection}
import com.typesafe.config.{Config, ConfigFactory}
import scala.concurrent.duration._

//TODO refactor into util object and constants.
//TODO Load constants from Akka config.
object SharedSpace {
  val masterSystemName = "MasterSys"
  val masterIP = "192.168.1.75"
  val masterPort = 2552
  val masterName = "theMaster"

  val cellarName = "theCellar"

  def getCellar(context: ActorContext): ActorSelection = context.actorSelection(s"akka.tcp://$masterSystemName@$masterIP:$masterPort/user/$cellarName")

  def getMaster(context: ActorContext): ActorSelection = context.actorSelection(s"akka.tcp://$masterSystemName@$masterIP:$masterPort/user/$masterName")

  val config: Config = ConfigFactory.load()

  val blockSize: Int = 100

  val workQueueCapacity = 30 //Should be like 10x of workerCount? Small values are advisable at first, but then n^2 grows so much that it doesn't matter. Small values create more duplicate jobs even at great network conditions.
  val workQueueThreshold: Int = workQueueCapacity / 3

  val pigTimeout: FiniteDuration = 2 hours //This timeout is not heartbeat-modulated, so it's best to set it big. We are using Akka for heartbeating.
  val slaveTimeout: FiniteDuration = 10 minutes //Basically the timeout of getting a data frame from the cellar.

  def getOffset(index: BigInt): BigInt = {
    2 + index * blockSize
  }

}
