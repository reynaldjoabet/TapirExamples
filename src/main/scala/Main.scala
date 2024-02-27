import sttp.tapir._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.clientIp
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.model.StatusCode
import sttp.tapir.server.http4s.Http4sServerOptions
import cats.effect.IO
import sttp.tapir.server.interceptor.decodefailure.DefaultDecodeFailureHandler

object Main extends App {

  Schema

  final case class HttpError(
      statusCode: StatusCode,
      message: String,
      cause: Throwable
  ) extends RuntimeException(message, cause)

  trait BaseEndpoint {

    val baseEndpoint = endpoint
      .errorOut(statusCode and plainBody[String])
    // .mapErrorOut[Throwable](x=>HttpError())(HttpError.encode)

    val secureEndpoint = baseEndpoint
      .securityIn(auth.bearer[String]())
  }

  val customServerOptions = Http4sServerOptions
    .customiseInterceptors[IO]
    .decodeFailureHandler(DefaultDecodeFailureHandler.hideEndpointsWithAuth[IO])
    .options

  Http4sServerInterpreter(customServerOptions) // .toRoutes( ???)
}
