import sttp.tapir._
import com.alejandrohdezma.tapir._
import sttp.tapir.generic.auto._
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.Schema
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.ConfiguredJsonCodec
import sttp.model.StatusCode._
import sttp.tapir.json.circe.jsonBody
object TapirAnyOf {

  // First create your error as you would normally do using Tapir:

  // import io.circe.generic.extras.Configuration
  // import io.circe.generic.extras.ConfiguredJsonCodec

  // @ConfiguredJsonCodec sealed trait MyError
  // case class UserNotFound(name: String) extends MyError
  // case class WrongUser(id: String) extends MyError
  // case class WrongPassword(id: String) extends MyError

  // object MyError {

  //   implicit val config: Configuration =
  //     Configuration.default.withDiscriminator("error")

  // }

  // Then you need to ensure that every error type has a `Schema` instance and that it is annotated with `@code` indicating the status code that should be used when that error is returned:

  @ConfiguredJsonCodec sealed trait MyError
  @code(NotFound) final case class UserNotFound(name: String) extends MyError
  @code(Forbidden) final case class WrongUser(id: String) extends MyError
  @code(Forbidden) final case class WrongPassword(id: String) extends MyError

  object MyError {
    implicit val codec: Codec[MyError] = deriveCodec[MyError]
    implicit val config: Configuration =
      Configuration.default.withDiscriminator("error")

    implicit lazy val MyErrorSchema: Schema[MyError] = Schema.derived[MyError]

  }


object anyOf extends AnyOf[MyError](jsonBody)
//Add discriminator information to `Schema`
  /* Populate the `sealed trait` schema with the discriminator information by calling `addDiscriminator` indicating the discriminator name. This will involve two things: first, adding the actual discriminator value to the `SCoproduct` itself and second, adding the discriminator as a field to every subtype of the coproduct. */

  endpoint.get
    .in("v1" / "users" / path[String]("id"))
    .out(stringBody)
    .errorOut(anyOf[UserNotFound, WrongPassword, WrongUser])

  object MyError5 {

    //  implicit val config: Configuration =
    //    Configuration.default.withDiscriminator("error")

    implicit lazy val MyErrorSchema: Schema[MyError] =
      Schema.derived[MyError].addDiscriminator("error")

  }

EndpointOutput.OneOf

}
