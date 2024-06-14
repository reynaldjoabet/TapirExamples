package domain
package data

import java.time.LocalDate

import sttp.tapir.Schema

final case class Airline(
  name: String,
  iataCode: IataCode, // TODO AirlineIataCode
  icaoCode: IcaoCode, // TODO AirlineIcaoCode
  foundationDate: LocalDate,
  country: Country
)

object Airline {
  implicit val schema: Schema[Airline] = Schema.derived[Airline]
}
