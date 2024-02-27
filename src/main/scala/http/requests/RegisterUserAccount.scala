package http
package requests
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec
final case class RegisterUserAccount(
    email: String,
    password: String
)

object RegisterUserAccount {
  implicit val registerUserAccountCodec: Codec.AsObject[RegisterUserAccount] =
    deriveCodec[RegisterUserAccount]
}
