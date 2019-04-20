name := "distap"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.5.22"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.22"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.5.22"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "org.roaringbitmap" % "RoaringBitmap" % "0.8.1"


//libraryDependencies += "com.twitter" %% "chill" % "0.9.1"
libraryDependencies += "com.esotericsoftware" % "kryo" % "5.0.0-RC4"

libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "1.6.6"
