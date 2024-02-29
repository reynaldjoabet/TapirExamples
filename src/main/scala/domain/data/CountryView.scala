package domain.data
import sttp.tapir.Schema
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.{Codec, Encoder, Decoder}

case class CountryView(
    code: CountryCode,
    name: String
)
object CountryView {
  implicit val decoder: Decoder[CountryView] = deriveDecoder[CountryView]
  implicit val encoder: Encoder[CountryView] = deriveEncoder[CountryView]
}
