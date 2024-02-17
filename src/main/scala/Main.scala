import sttp.tapir._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.clientIp
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.model.StatusCode

object Main extends App {
  println("Hello, World!")

final case class HttpError(
    statusCode: StatusCode,
    message: String,
    cause: Throwable
) extends RuntimeException(message, cause)

  trait BaseEndpoint {

    val baseEndpoint = endpoint
      .errorOut(statusCode and plainBody[String])
      //.mapErrorOut[Throwable](x=>HttpError())(HttpError.encode)

    val secureEndpoint = baseEndpoint
      .securityIn(auth.bearer[String]())
  }
}
