package domain
package data

import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec

final case class User(
  id: Long,
  email: String,
  hashedPassword: String
) {
  def toUserId = UserId(id, email)
}

final case class UserId(
  id: Long,
  email: String
)

object UserId {
  implicit val userIdCodec: Codec.AsObject[UserId] = deriveCodec[UserId]
}

object User {
  implicit val userCodec: Codec.AsObject[User] = deriveCodec[User]
}
