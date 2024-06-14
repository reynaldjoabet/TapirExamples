package http.endpoints

import domain.errors.HttpError
import sttp.tapir._
import sttp.tapir.json.circe._

trait BaseEndpoint {

  val baseEndpoint: Endpoint[Unit, Unit, Throwable, Unit, Any] = endpoint
    .errorOut(statusCode.and(plainBody[String]))
    .mapErrorOut[Throwable](HttpError.decode(_))(HttpError.encode)

  val secureBaseEndpoints: Endpoint[String, Unit, Throwable, Unit, Any] =
    baseEndpoint.securityIn(auth.bearer[String]())

}
