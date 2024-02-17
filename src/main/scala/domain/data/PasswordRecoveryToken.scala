package com.rockthejvm.reviewboard.domain.data
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec
final case class PasswordRecoveryToken(
    email: String,
    token: String,
    expiration: Long
)
object PasswordRecoveryToken{
    implicit val codec: Codec.AsObject[PasswordRecoveryToken]= deriveCodec[PasswordRecoveryToken]
}
