package domain

import java.util.UUID
import cats.kernel.Order
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec
package object data {

// A language code format according to ISO 639-1. Please note that this only verifies the format!
  type LanguageCode = String // Refined MatchesRegex["^[a-z]{2}$"]
object LanguageCode{
    def apply(value:String):LanguageCode=value
}
  type ProductId = UUID
  object ProductId{
    def apply(value:UUID):ProductId=value
  }
  // A product name must be a non-empty string.
  type ProductName = String // Refined NonEmpty
  object ProductName{
    def apply(value:String):ProductName=value
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

}
