
package http
package requests
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec
final case class LoginRequest(
    email: String,
    password: String
) 

object LoginRequest{
    implicit val loginRequestCodec: Codec.AsObject[LoginRequest]= deriveCodec[LoginRequest]
}
