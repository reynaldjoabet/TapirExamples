package domain.data

import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import sttp.tapir.Schema

case class CountryView(
  code: CountryCode,
  name: String
)

object CountryView {

  implicit val decoder: Decoder[CountryView] = deriveDecoder[CountryView]
  implicit val encoder: Encoder[CountryView] = deriveEncoder[CountryView]

}
