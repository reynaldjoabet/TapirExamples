package http
package requests

import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec

case class RecoverPasswordRequest(
  email: String,
  token: String,
  newPassword: String
)

object RecoverPasswordRequest {

  implicit val recoverPasswordRequestCodec: Codec.AsObject[RecoverPasswordRequest] =
    deriveCodec[RecoverPasswordRequest]

}
