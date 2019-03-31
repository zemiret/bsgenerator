name := "bsgenerator"

version := "0.1"

scalaVersion := "2.12.8"

lazy val akkaVersion = "2.5.21"
lazy val akkaHttpVersion = "10.1.8"
lazy val akkaStreamVersion = "2.5.21"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion, 
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
)
