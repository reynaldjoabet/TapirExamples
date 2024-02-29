package domain
package errors
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.{Codec, Encoder, Decoder}

/* ERROR MODEL */
sealed trait ErrorInfo

object ErrorInfo {
  case class NotFound(info: String) extends ErrorInfo
  case class Conflict(info: String) extends ErrorInfo
  case class Unauthorized(info: String) extends ErrorInfo
  case class Unknown(info: String) extends ErrorInfo
  case object NoContent extends ErrorInfo

  object NotFound {
    implicit val decoder: Decoder[NotFound] = deriveDecoder[NotFound]
    implicit val encoder: Encoder[NotFound] = deriveEncoder[NotFound]
  }
  object Conflict {
    implicit val decoder: Decoder[Conflict] = deriveDecoder[Conflict]
    implicit val encoder: Encoder[Conflict] = deriveEncoder[Conflict]
  }
  object Unauthorized {
    implicit val decoder: Decoder[Unauthorized] = deriveDecoder[Unauthorized]
    implicit val encoder: Encoder[Unauthorized] = deriveEncoder[Unauthorized]
  }

  object Unknown {
    implicit val decoder: Decoder[Unknown] = deriveDecoder[Unknown]
    implicit val encoder: Encoder[Unknown] = deriveEncoder[Unknown]
  }
}





