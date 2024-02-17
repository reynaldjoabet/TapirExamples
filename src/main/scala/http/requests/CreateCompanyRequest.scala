package http
package requests
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec
final case class CreateCompanyRequest(
    name: String,
    url: String,
    location: Option[String] = None,
    country: Option[String] = None,
    industry: Option[String] = None,
    image: Option[String] = None,
    tags: Option[List[String]] = None
) {
  // def toCompany(id: Long): Company =
  //   Company(id, Company.makeSlug(name), name, url, location, country, industry, image, tags.getOrElse(List.empty))
}
object CreateCompanyRequest{
  implicit val createCompanyRequestCodec: Codec.AsObject[CreateCompanyRequest]= deriveCodec[CreateCompanyRequest]
}


