package domain
package errors

import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec

abstract class ApplicationException(message: String) extends RuntimeException(message)

case object UnAuthorizedException extends ApplicationException("Unauthorized")
