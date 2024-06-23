// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.9.9",
  // https://mvnrepository.com/artifact/com.softwaremill.sttp.tapir/tapir-json-circe
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe"     % "1.9.9",
  "io.circe"                    %% "circe-generic-extras" % "0.14.3",
  "com.alejandrohdezma"         %% "tapir-anyof"          % "0.7.0"
)

scalacOptions += "-Ymacro-annotations"
fork in run   := true

ThisBuild / semanticdbEnabled := true
