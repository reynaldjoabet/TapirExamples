package domain
package data
import sttp.tapir.Schema
final case class Airport(
    name: String,
    iataCode: IataCode,
    icaoCode: IcaoCode,
    country: Country
)
object Airport {
  implicit val schema: Schema[Airport] = Schema.derived[Airport]
}
