import scala.annotation.StaticAnnotation
import scala.concurrent.ExecutionContext

import cats.effect.IO
import fs2._

import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.server.Router
import org.http4s.HttpRoutes
import sttp.capabilities.fs2.Fs2Streams
import sttp.capabilities.WebSockets
import sttp.tapir._
import sttp.tapir.server.http4s.Http4sServerInterpreter

implicit val ec: ExecutionContext =
  scala.concurrent.ExecutionContext.Implicits.global

val wsEndpoint
  : PublicEndpoint[Unit, Unit, Pipe[IO, String, String], Fs2Streams[IO] with WebSockets] =
  endpoint
    .get
    .in("count")
    .out(
      webSocketBody[
        String,
        CodecFormat.TextPlain,
        String,
        CodecFormat.TextPlain
      ](Fs2Streams[IO])
    )

val wsRoutes: WebSocketBuilder2[IO] => HttpRoutes[IO] =
  Http4sServerInterpreter[IO]().toWebSocketRoutes(
    wsEndpoint.serverLogicSuccess[IO](_ => ???)
  )

class Author(name: String) extends StaticAnnotation

@Author("John Doe")
class MyClass {
  // class definition
}

val m = new MyClass()
