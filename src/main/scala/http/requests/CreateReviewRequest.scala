package http
package requests

import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec

final case class CreateReviewRequest(
  companyId: Long,
  management: Int,
  culture: Int,
  salary: Int,
  benefits: Int,
  wouldRecommend: Int,
  review: String
)

object CreateReviewRequest {

  implicit val createReviewRequestCodec: Codec.AsObject[CreateReviewRequest] =
    deriveCodec[CreateReviewRequest]

}
