package domain
package data
//models
import cats.Order
import cats.data.NonEmptySet
import cats.implicits._
// import eu.timepit.refined.auto._
import io.circe._
import io.circe.generic.semiauto._
import sttp.tapir._
//import io.circe.Decoder._
import sttp.tapir.generic.auto._

/** A product.
  *
  * @param id
  *   The unique ID of the product.
  * @param names
  *   A list of translations of the product name.
  */
final case class Product(id: ProductId, names: NonEmptySet[Translation])

object Product {

  implicit val decode: Decoder[Product] =
    Decoder[Product] // deriveDecoder[Product]

  implicit val encode: Encoder[Product] = deriveEncoder[Product]

  implicit val order: Order[Product] = new Order[Product] {
    def compare(x: Product, y: Product): Int = x.id.compare(y.id)
  }

  implicit val schemaForProductId: Schema[ProductId] = Schema.string

  implicit def schemaForNonEmptySet[T](implicit
      a: Schema[T]
  ): Schema[NonEmptySet[T]] =
    Schema(SchemaType.SArray(a)(_.toIterable))

  implicit val translationSchema: Schema[Translation] =
    Schema.derived[Translation]
  implicit val translationSchemaSet: Schema[NonEmptySet[Translation]] =
    schemaForNonEmptySet[Translation]

    //translationSchema.validate()
  /** magnolia: could not find Schema.Typeclass for type
    * cats.data.NonEmptySet[domain.data.Translation] in parameter 'names' of
    * product type domain.data.Product
    */
  implicit val schemaFor: Schema[Product] = Schema.derived[Product]

  /** Try to create a Product from the given list of database rows.
    *
    * @param rows
    *   The database rows describing a product and its translations.
    * @return
    *   An option to the successfully created Product.
    */
  def fromDatabase(rows: Seq[(ProductId, LanguageCode, ProductName)]): Option[Product] = {
    val po = for {
      (id, c, n) <- rows.headOption
      t = Translation(lang = c, name = n)
      p <- Product(id = id, names = NonEmptySet.one(t)).some
    } yield p
    po.map(p =>
      rows.drop(1).foldLeft(p) { (a, cols) =>
        val (_, c, n) = cols
        a.copy(names = a.names.add(Translation(lang = c, name = n)))
      }
    )
  }
}
