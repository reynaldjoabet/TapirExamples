import cats.effect.IO

import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.clientIp
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.interceptor.decodefailure.DefaultDecodeFailureHandler

object Main extends App {

  Schema

  final case class HttpError(
    statusCode: StatusCode,
    message: String,
    cause: Throwable
  ) extends RuntimeException(message, cause)

  trait BaseEndpoint {

    val baseEndpoint = endpoint.errorOut(statusCode.and(plainBody[String]))
    // .mapErrorOut[Throwable](x=>HttpError())(HttpError.encode)

    val secureEndpoint = baseEndpoint.securityIn(auth.bearer[String]())

  }

  val customServerOptions = Http4sServerOptions
    .customiseInterceptors[IO]
    .decodeFailureHandler(DefaultDecodeFailureHandler.hideEndpointsWithAuth[IO])
    .options

  Http4sServerInterpreter(customServerOptions) // .toRoutes( ???)

  import io.circe.generic.auto._
  import sttp.model.StatusCode
  import sttp.tapir._
  import sttp.tapir.generic.auto._
  import sttp.tapir.json.circe._

  sealed trait ErrorInfo
  case class NotFound(what: String)          extends ErrorInfo
  case class Unauthorized(realm: String)     extends ErrorInfo
  case class Unknown(code: Int, msg: String) extends ErrorInfo
  case object NoContent                      extends ErrorInfo

  // here we are defining an error output, but the same can be done for regular outputs
  val baseEndpoint = endpoint.errorOut(
    oneOf[ErrorInfo](
      oneOfVariant(
        statusCode(StatusCode.NotFound).and(jsonBody[NotFound].description("not found"))
      ),
      oneOfVariant(
        statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized].description("unauthorized"))
      ),
      oneOfVariant(
        statusCode(StatusCode.NoContent).and(emptyOutputAs(NoContent))
      ),
      oneOfDefaultVariant(jsonBody[Unknown].description("unknown"))
    )
  )

}
