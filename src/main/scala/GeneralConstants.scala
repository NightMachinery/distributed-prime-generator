import akka.actor.{ActorContext, ActorSelection}
import com.typesafe.config.{Config, ConfigFactory}
import scala.concurrent.duration._

object GeneralConstants {
  val masterSystemName = "MasterSys"
  val masterIP = "127.0.0.1"
  val masterPort = 2552
  val masterName = "theMaster"

  val cellarName = "theCellar"

  def getCellar(context: ActorContext): ActorSelection = context.actorSelection(s"akka.tcp://$masterSystemName@$masterIP:$masterPort/user/$cellarName")

  def getMaster(context: ActorContext): ActorSelection = context.actorSelection(s"akka.tcp://$masterSystemName@$masterIP:$masterPort/user/$masterName")

  val config: Config = ConfigFactory.load()

  val blockSize: Int = 100

  val workQueueCapacity = 10
  val workQueueThreshold: Int = workQueueCapacity / 3

  val pigTimeout: FiniteDuration = 2 hours //This timeout is not heartbeat-modulated, so it's best to set it big. We are using Akka for heartbeating.
  val slaveTimeout: FiniteDuration = 10 minutes

}
