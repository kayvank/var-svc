package utils

import java.util.Optional
import cats.syntax.either._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}
import cats.implicits._
import cats.instances._
import scalaz._, Scalaz._
import scalaz.concurrent.Task

object FailedComputationWrapper {

  implicit class TryToTask(t: Try[_]) {

    def asTask[_](): Task[_] = t match {
      case Success(_) => Task.now(t.get)
      case Failure(e) => Task.fail(e)
    }
  }
}
