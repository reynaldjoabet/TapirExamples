// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.9.9",
  // https://mvnrepository.com/artifact/com.softwaremill.sttp.tapir/tapir-json-circe
   "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.9.9"

)

fork in run := true
