import cats.effect.IO

import org.http4s.HttpRoutes
import sttp.tapir._
import sttp.tapir.server.http4s.Http4sServerInterpreter

def countCharacters(s: String): IO[Either[Unit, Int]] =
  IO.pure(Right[Unit, Int](s.length))

IO(23).attempt

val countCharactersEndpoint: PublicEndpoint[String, Unit, Int, Any] =
  endpoint.in(stringBody).out(plainBody[Int])

val countCharactersRoutes: HttpRoutes[IO] =
  Http4sServerInterpreter[IO]().toRoutes(
    countCharactersEndpoint.serverLogic(countCharacters _)
  )
