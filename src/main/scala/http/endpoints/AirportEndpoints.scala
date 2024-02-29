package http.endpoints
import sttp.tapir._
import domain.errors._
import domain.data._
import sttp.tapir.json.circe._
import sttp.model.{StatusCode,HeaderNames}
import domain.errors.ErrorInfo
import sttp.tapir.generic.auto._
import domain.errors.ProgramError.{
    CountryNotFound,
    DuplicateEntityError,
    ServiceError
  }
import domain.errors.ErrorInfo.{
    Conflict,
    NotFound,
    Unauthorized,
    Unknown
  }

object AirportEndpoints {
  lazy val airportsResource = "airports"
  lazy val airportPath = airportsResource
  lazy val iataCodePath = path[String]("iataCode")

def manageError(serviceError: ServiceError): ErrorInfo =
    serviceError match {
      case CountryNotFound(code)        => NotFound(code)
      case DuplicateEntityError(entity) => Conflict(entity)
      case _                            => Unknown("Service Error")
    }



  val commonMappings = List(
    oneOfVariant(
      statusCode(StatusCode.Unauthorized)
        .and(jsonBody[Unauthorized].description("unauthorized"))
    ),
    oneOfDefaultVariant(jsonBody[Unknown].description("service error"))
  )

  lazy val getByIataCodeEndpoint
      : PublicEndpoint[String, ErrorInfo, AirportView, Any] =
    endpoint.get
      .name("get-by-iata-code-endpoint")
      .description("Retrieves an Airport by its iata code")
      .in(airportPath)
      .in(iataCodePath)
      .out(jsonBody[AirportView].example(airportViewExample))
      .errorOut(
        oneOf[ErrorInfo](
          oneOfVariant(
            statusCode(StatusCode.NotFound)
              .and(jsonBody[NotFound].description("not found"))
          ),
          commonMappings: _*
        )
      )

  lazy val postEndpoint
      : PublicEndpoint[AirportView, ErrorInfo, (String, AirportView), Any] =
    endpoint.post
      .name("post-endpoint")
      .description("Creates an Airport")
      .in(airportPath)
      .in(jsonBody[AirportView].example(airportViewExample))
      .out(
        statusCode(StatusCode.Created)
          .and(header[String](HeaderNames.ContentLocation))
          .and(jsonBody[AirportView])
      )
      .errorOut(
        oneOf[ErrorInfo](
          oneOfVariant(
            statusCode(StatusCode.NoContent).and(emptyOutputAs(ErrorInfo.NoContent))
          ),
          commonMappings: _*
        )
      )

  lazy val countryPutEndpoint
      : PublicEndpoint[AirportView, ErrorInfo, AirportView, Any] =
    endpoint.put
      .name("put-endpoint")
      .description("Updates an Airport")
      .in(airportPath)
      .in(jsonBody[AirportView].example(airportViewExample))
      .out(
        statusCode(StatusCode.Ok)
          .and(jsonBody[AirportView].example(airportViewExample))
      )
      .errorOut(
        oneOf[ErrorInfo](
          oneOfVariant(
            statusCode(StatusCode.NoContent).and(emptyOutputAs(ErrorInfo.NoContent))
          ),
          commonMappings: _*
        )
      )

  lazy val deleteEndpoint: PublicEndpoint[String, ErrorInfo, Unit, Any] =
    endpoint.delete
      .name("delete-endpoint")
      .description("Deletes an Airport by its iata code")
      .in(airportPath)
      .in(iataCodePath)
      .out(statusCode(StatusCode.NoContent))
      .errorOut(
        oneOf[ErrorInfo](
          oneOfVariant(
            statusCode(StatusCode.NotFound)
              .and(jsonBody[NotFound].description("not found"))
          ),
          commonMappings: _*
        )
      )

  lazy val airportViewExample =
    AirportView("Madrid Barajas", "MAD", "LEMD", "es")
}
