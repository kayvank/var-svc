package model


object Exceptions {

  case class InvalidVideoId(message: String)
    extends Exception(message)

  case class NoVideoFoundException(message: String)
    extends Exception(message)

  case class UserComputationException(
    message: String = "User-token computation failed.") extends Exception(message)

  case class CountryComputationException(
    message: String = "Country computation failed.") extends Exception(message)

  case class RequestMaxSizeException(
    message: String) extends Throwable(message)

}
