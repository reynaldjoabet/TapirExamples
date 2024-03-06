package http.endpoints

import sttp.tapir._
import sttp.tapir.generic.auto._
import http.responses._
import http.requests._
import domain.data._
import sttp.tapir.json.circe._
import sttp.tapir.json.circe.jsonBody

trait ReviewEndpoints extends BaseEndpoint {

  val createReviewEndpoint
      : Endpoint[String, CreateReviewRequest, Throwable, Review, Any] =
    secureBaseEndpoints
      .tag("reviews")
      .name("create")
      .description("Create a review for a company")
      .in("reviews")
      .post
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])

  val getReviewByIdEndpoint
      : Endpoint[Unit, Long, Throwable, Option[Review], Any] = baseEndpoint
    .tag("reviews")
    .name("getById")
    .description("Get a review by its id")
    .in("reviews" / path[Long]("id"))
    .get
    .out(jsonBody[Option[Review]])

  val getByCompanyIdEndpoint
      : Endpoint[Unit, Long, Throwable, List[Review], Any] = baseEndpoint
    .tag("reviews")
    .name("getByCompanyId")
    .description("Get a review for a company")
    .in("reviews" / "companies" / path[Long]("id"))
    .get
    .out(jsonBody[List[Review]])

}
