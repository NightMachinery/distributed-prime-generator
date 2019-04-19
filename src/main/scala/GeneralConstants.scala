import com.typesafe.config.{Config, ConfigFactory}

object GeneralConstants {
  val masterSystemName = "MasterSys"
  val masterIP = "127.0.0.1"
  val masterPort = 2552
  val masterName = "theMaster"
  val config: Config = ConfigFactory.load()
}
