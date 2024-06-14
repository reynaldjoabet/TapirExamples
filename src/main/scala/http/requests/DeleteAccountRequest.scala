package http
package requests

import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec

final case class DeleteAccountRequest(
  email: String,
  password: String
)

object DeleteAccountRequest {

  implicit val deleteAccountRequestCodec: Codec.AsObject[DeleteAccountRequest] =
    deriveCodec[DeleteAccountRequest]

}
