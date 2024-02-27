package domain
package data
import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec

import java.time.Instant

final case class Review(
    id: Long, // PK
    companyId: Long,
    userId: Long, // FK
    management: Int, // 1-5
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String,
    created: Instant,
    updated: Instant
)

object Review {
  implicit val reviewCodec: Codec.AsObject[Review] = deriveCodec[Review]
}
