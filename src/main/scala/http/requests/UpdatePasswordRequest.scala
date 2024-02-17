package http
package requests
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec
final case class UpdatePasswordRequest(email: String, oldPassword: String, newPassword: String) 

object UpdatePasswordRequest{
    implicit val updatePasswordRequestCodec: Codec.AsObject[UpdatePasswordRequest]= deriveCodec[UpdatePasswordRequest]
}
