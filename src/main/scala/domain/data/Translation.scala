package domain
package data

import cats._
import cats.Order

// import cats.derived
import io.circe._
import io.circe.generic.semiauto._
import sttp.tapir.Schema

/**
  * The translation data for a product name.
  *
  * @param lang
  *   A language code specifying the target translation.
  * @param name
  *   The product name in the language.
  */
final case class Translation(lang: LanguageCode, name: ProductName)

object Translation {

  implicit val decode: Decoder[Translation] = deriveDecoder[Translation]

  implicit val encode: Encoder[Translation] = deriveEncoder[Translation]

//   implicit val order: Order[Translation] = {
//     import derived.auto.order._
//     derived.semiauto.order[Translation]
//   }
  implicit val order: Order[Translation] = new Order[Translation] {

    override def compare(x: Translation, y: Translation): Int =
      x.lang.compare(y.lang)

  }

// we can generate a Codec based on the Schema
// we can generate binaryCodec, Json Codec based on the Schema
  implicit val schemaForLanguageCode: Schema[LanguageCode] = Schema.string
  implicit val schemaForProductName: Schema[ProductName]   = Schema.string
  // implicit val schemaFor: Schema[Translation]              = Schema.derived[Translation]

}
