package api

import java.util.NoSuchElementException
import javax.security.sasl.AuthenticationException
import com.vevo.tokens.error.TokenException
import model.Exceptions._
import org.http4s._
import org.http4s.dsl._
import scalaz.concurrent.Task
import utils.Monitor._

object Ex {
  val throwbe2response: PartialFunction[Throwable, Task[Response]] = {
    case e: AuthenticationException =>
      counter400s.increment(System.currentTimeMillis)
      Unauthorized(Challenge("", "", Map()))
    case e: TokenException =>
      counter400s.increment()
      BadRequest(e.getMessage)
    case e: IllegalArgumentException =>
      counter400s.increment()
      BadRequest(e.getMessage)
    case e: CountryComputationException =>
      counter400s.increment()
      BadRequest(e.getMessage)
    case e: UserComputationException =>
      counter400s.increment()
      BadRequest(e.getMessage)
    case e: NoSuchElementException =>
      counter400s.increment()
      NotFound(e.getMessage)
    case e: InvalidVideoId =>
      counter400s.increment()
      NotFound(e.getMessage)
    case e: RequestMaxSizeException =>
      counter400s.increment()
      BadRequest(e.getMessage)
    case e: Throwable =>
      counter500s.increment()
      InternalServerError(e.getMessage)
  }
}
