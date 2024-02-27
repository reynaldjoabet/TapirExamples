package domain
package data
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec

final case class Company(
    id: Long,
    slug: String,
    name: String,
    url: String,
    location: Option[String] = None,
    country: Option[String] = None,
    industry: Option[String] = None,
    image: Option[String] = None,
    tags: List[String] = List.empty
)

object Company {
  implicit val codec: Codec.AsObject[Company] = deriveCodec[Company]
}
