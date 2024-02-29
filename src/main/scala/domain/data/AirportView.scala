package domain
package data

import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.{Codec, Encoder, Decoder}
import sttp.tapir.Schema
case class AirportView(
    name: String,
    iataCode: String,
    icaoCode: String,
    airportCode: String
)
object AirportView {
  implicit val decoder: Decoder[AirportView] = deriveDecoder[AirportView]
  implicit val encoder: Encoder[AirportView] = deriveEncoder[AirportView]
  implicit val schema: Schema[AirportView] = Schema.derived[AirportView]
}
