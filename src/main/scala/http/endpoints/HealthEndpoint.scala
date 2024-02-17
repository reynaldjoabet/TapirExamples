package http.endpoints

import sttp.tapir._
import sttp.tapir.generic.auto._
import http.responses._
trait HealthEndpoint extends BaseEndpoint {

  val healthEndpoint: Endpoint[Unit, Unit, Throwable, String, Any] = baseEndpoint
    .tag("health")
    .name("health")
    .description("Health check")
    .get
    .in("health")
    .out(plainBody[String])

  val errorEndpoint: Endpoint[Unit, Unit, Throwable, String, Any] = baseEndpoint
    .tag("health")
    .name("error health")
    .description("Health check - should fail")
    .get
    .in("health" / "error")
    .out(plainBody[String])

}
