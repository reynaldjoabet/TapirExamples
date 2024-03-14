package domain
package errors

trait ProgramError
object ProgramError {
  sealed trait ServiceError extends ProgramError
  trait RepositoryError extends ProgramError

  case class MissingEntityError(message: String) extends ServiceError
  case class DuplicateEntityError(message: String) extends ServiceError
  case class UnexpectedServiceError(message: String) extends ServiceError
  case class CountryNotFound(code: String) extends ServiceError
  // case class DuplicateEntityError1(text: String) extends ServiceError
  case class DefaultError(text: String) extends ServiceError

}
