package domain.data
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec

final case class UserToken(
    email: String,
    token: String,
    expires: Long
) 
object UserToken{
    implicit val userTokenCodec: Codec.AsObject[UserToken]= deriveCodec[UserToken]
}
