package domain
package data
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec
import sttp.tapir.Schema

final case class UserToken(
    email: String,
    token: String,
    expires: Long
)
object UserToken {
  Schema
  implicit val userTokenCodec: Codec.AsObject[UserToken] =
    deriveCodec[UserToken]
}
