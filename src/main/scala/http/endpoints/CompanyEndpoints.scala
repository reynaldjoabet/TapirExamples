package http.endpoints

import domain.data._
import http.requests._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

trait CompanyEndpoints extends BaseEndpoint {

  val createEndpoint: Endpoint[String, CreateCompanyRequest, Throwable, Company, Any] =
    secureBaseEndpoints
      .tag("companies")
      .name("create")
      .description("Create a listing for a company")
      .in("companies")
      .post
      .in(jsonBody[CreateCompanyRequest])
      .out(jsonBody[Company])

  val getAllEndpoint: Endpoint[Unit, Unit, Throwable, List[Company], Any] =
    baseEndpoint
      .tag("companies")
      .name("getAll")
      .description("Get all company listing")
      .in("companies")
      .get
      .out(jsonBody[List[Company]])

  val getByIdEndpoint: Endpoint[Unit, String, Throwable, Option[Company], Any] =
    baseEndpoint
      .tag("companies")
      .name("getById")
      .description("Get a company by its id")
      .in("companies" / path[String]("id"))
      .get
      .out(jsonBody[Option[Company]])

}
