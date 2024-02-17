
package http
package responses
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec
final case class UserResponse(email: String) 
object UserResponse{
    implicit val userResponseCodec: Codec.AsObject[UserResponse]= deriveCodec[UserResponse]
}
