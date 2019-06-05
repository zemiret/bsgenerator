import java.nio.file.{FileSystems, Files, StandardCopyOption}

name := "bsgenerator"

version := "0.1"

scalaVersion := "2.12.8"

lazy val akkaVersion = "2.5.21"
lazy val akkaHttpVersion = "10.1.8"
lazy val akkaStreamVersion = "2.5.21"
lazy val jSoupVersion = "1.11.3"
lazy val nd4jVersion = "1.0.0-beta4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "org.jsoup" % "jsoup" % jSoupVersion,
  "org.scalikejdbc" %% "scalikejdbc" % "3.3.+",
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "org.deeplearning4j" % "deeplearning4j-core" % nd4jVersion,
  "org.nd4j" % "nd4j-x86" % "0.4-rc3.8",
  "org.nd4j" % "nd4j-native-platform" % nd4jVersion,
  "org.nd4j" % "nd4j-native" % nd4jVersion
)

lazy val configCopyTask = taskKey[Unit]("Config copy task")
configCopyTask := {
  println("Copying config...")
  val filename = "application.conf"
  val classDir = (Compile / packageBin / classDirectory).value.toPath

  Files.createDirectories(classDir)

  Files.copy(
    FileSystems.getDefault.getPath(filename),
    new File(classDir.toString, filename).toPath,
    StandardCopyOption.REPLACE_EXISTING
  )
  println("Copying config done.")
}

(Compile / compile) := ((Compile / compile) dependsOn configCopyTask).value

Compile / compileOrder := CompileOrder.JavaThenScala

