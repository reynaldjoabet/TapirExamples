package domain

import java.util.UUID
import cats.kernel.Order
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.{Codec, Encoder, Decoder}
import sttp.tapir._
package object data {

// A language code format according to ISO 639-1. Please note that this only verifies the format!
  type LanguageCode = String // Refined MatchesRegex["^[a-z]{2}$"]
  object LanguageCode {
    def apply(value: String): LanguageCode = value
  }
  type ProductId = UUID
  object ProductId {
    def apply(value: UUID): ProductId = value
  }
  // A product name must be a non-empty string.
  type ProductName = String // Refined NonEmpty
  object ProductName {
    def apply(value: String): ProductName = value
  }
  // object ProductName extends RefinedTypeOps[ProductName, String] with CatsRefinedTypeOpsSyntax

  implicit val orderLanguageCode: Order[LanguageCode] =
    new Order[LanguageCode] {
      def compare(x: LanguageCode, y: LanguageCode): Int = x.compare(y)
    }

  implicit val orderProductId: Order[ProductId] = new Order[ProductId] {
    def compare(x: ProductId, y: ProductId): Int =
      x.toString.compare(y.toString)
  }

  implicit val orderProductName: Order[ProductName] = new Order[ProductName] {
    def compare(x: ProductName, y: ProductName): Int = x.compare(y)
  }

  // object CountryCode  {
  //         implicit val encoder: Encoder[CountryCode] =
  //           Encoder[String].contramap(_.toString())
  //         implicit val decoder: Decoder[CountryCode] =
  //           Decoder[String].map(code => CountryCode)
  //         implicit val schema: Schema[CountryCode]       =
  //           Schema(SchemaType.SString())

  //       }

//type CountryCode = CountryCode.type
  type CountryCode = String

//  object IataCode
//  type IataCode = IataCode.type

  type IataCode = String
//  object IcaoCode
//  type IcaoCode = IcaoCode.type

  type IcaoCode = String

}
