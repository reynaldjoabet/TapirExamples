package http
package requests

import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec

final case class ForgotPasswordRequest(
  email: String
)

object ForgotPasswordRequest {

  implicit val forgotPasswordRequestCodec: Codec.AsObject[ForgotPasswordRequest] =
    deriveCodec[ForgotPasswordRequest]

}
