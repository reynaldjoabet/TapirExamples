import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future
import sttp.tapir._
import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global
case class User(name: String)
case class AuthenticationToken(value: String)
case class AuthenticationError(code: Int)

def authenticate(token: AuthenticationToken) =
  Future[Either[AuthenticationError, User]] {
    if (token.value == "berries") Right(User("Papa Smurf"))
    else if (token.value == "smurf") Right(User("Gargamel"))
    else Left(AuthenticationError(1001))
  }

val secureEndpoint = endpoint
  .securityIn(auth.bearer[String]().mapTo[AuthenticationToken])
  // returning the authentication error code to the user
  .errorOut(plainBody[Int].mapTo[AuthenticationError])
  .serverSecurityLogic(authenticate)

// the errors that might occur in the /hello endpoint -
// either a wrapped authentication error, or refusal to greet
object HelloError {
  sealed trait HelloError
  case class AuthenticationHelloError(wrapped: AuthenticationError)
      extends HelloError
  case class NoHelloError(val why: String) extends HelloError
}
import HelloError._

// extending the base endpoint with hello-endpoint-specific inputs
val secureHelloWorldWithLogic: ServerEndpoint[Any, Future] =
  secureEndpoint.get
    .in("hello")
    .in(query[String]("salutation"))
    .out(stringBody)
    .mapErrorOut(AuthenticationHelloError)(_.wrapped)
    // returning a 400 with the "why" field from the exception
    .errorOutVariant[HelloError](oneOfVariant(stringBody.mapTo[NoHelloError]))
    // defining the remaining server logic
    // (which uses the authenticated user)
    .serverLogic { user => salutation =>
      Future(
        if (user.name == "Gargamel")
          Left(NoHelloError(s"Not saying hello to ${user.name}!"))
        else Right(s"$salutation, ${user.name}!")
      )
    }
