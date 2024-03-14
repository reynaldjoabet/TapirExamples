package http.endpoints
import sttp.tapir._
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server._
import sttp.tapir.{EndpointInput, PublicEndpoint, Schema, SchemaType}
import cats.effect._
import domain.data._
import domain.errors.ErrorInfo
import sttp.tapir.generic.auto._
import domain.errors.ProgramError.{
  CountryNotFound,
  DuplicateEntityError,
  ServiceError
}
import domain.errors.ErrorInfo.{Conflict, NotFound, Unauthorized, Unknown}
import sttp.tapir.json.circe._

object CountryEndpoints {
  // ServiceError => ApiError
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

  lazy val countriesResource = "countries"
  lazy val countryPath: EndpointInput[Unit] = countriesResource
  lazy val codePath = path[String]("code").description("Country code")

  val getEndpoint: PublicEndpoint[String, ErrorInfo, CountryView, Any] =
    endpoint.get
      .name("get-by-code-endpoint")
      .description("Retrieves a Country by its code")
      .in(countryPath)
      .in(codePath)
      .out(jsonBody[CountryView].example(countryViewExample))
      .errorOut(
        oneOf[ErrorInfo](
          oneOfVariant(
            statusCode(StatusCode.NotFound)
              .and(jsonBody[NotFound].description("resource not found"))
          ),
          commonMappings: _*
        )
      )

  val getAllEndpoint: PublicEndpoint[Unit, ErrorInfo, Seq[CountryView], Any] =
    endpoint.get
      .name("get-all-endpoint")
      .description("Retrieves a finite Country sequence")
      .in(countryPath)
      .out(jsonBody[Seq[CountryView]].example(countryViewSeqExample))
      .errorOut(
        oneOf[ErrorInfo](
          oneOfVariant(
            statusCode(StatusCode.NotFound)
              .and(jsonBody[NotFound].description("not found"))
          ),
          commonMappings: _*
        )
      )

  lazy val postEndpoint: PublicEndpoint[CountryView, ErrorInfo, String, Any] =
    endpoint.post
      .name("post-endpoint")
      .description("Creates a Country")
      .in(countryPath)
      .in(jsonBody[CountryView].example(countryViewExample))
      .out(
        statusCode(StatusCode.Created)
          .and(header[String](HeaderNames.ContentLocation))
          // .and(jsonBody[CountryView])
      )
      .errorOut(
        oneOf[ErrorInfo](
          oneOfVariant(
            statusCode(StatusCode.NoContent)
              .and(emptyOutputAs(ErrorInfo.NoContent))
          ),
          (oneOfVariant(
            statusCode(StatusCode.Conflict)
              .and(jsonBody[Conflict].description("duplicated"))
          ) +: commonMappings): _*
        )
      )

  lazy val putEndpoint
      : PublicEndpoint[CountryView, ErrorInfo, CountryView, Any] =
    endpoint.put
      .name("put-endpoint")
      .description("Updates a Country")
      .in(countryPath)
      .in(jsonBody[CountryView].example(countryViewExample))
      .out(
        statusCode(StatusCode.Ok)
          .and(jsonBody[CountryView].example(countryViewExample))
      )
      .errorOut(
        oneOf[ErrorInfo](
          oneOfVariant(
            statusCode(StatusCode.NoContent)
              .and(emptyOutputAs(ErrorInfo.NoContent))
          ),
          commonMappings: _*
        )
      )

  lazy val deleteEndpoint: PublicEndpoint[String, ErrorInfo, Unit, Any] =
    endpoint.delete
      .name("delete-endpoint")
      .description("Deletes a Country by its code")
      .in(countryPath)
      .in(codePath)
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

  lazy val countryViewExample = CountryView("es", "Spain")
  lazy val ptCountryViewExample = CountryView("pt", "Portugal")
  lazy val countryViewSeqExample = Seq(countryViewExample, ptCountryViewExample)
}
