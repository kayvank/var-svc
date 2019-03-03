package utils

import java.util.Optional

import cats.syntax.either._

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}
import cats.implicits._
import cats.instances._

import scalaz.{-\/, \/, \/-}

/**
  * Conversions between Scala Option and Java 8 Optional.
  */
object JavaOptionals {
  implicit def toRichOption[T](opt: Option[T]): RichOption[T] = new RichOption[T](opt)

  implicit def toRichOptional[T](optional: Optional[T]): RichOptional[T] = new RichOptional[T](optional)

  implicit def eitherToTry[A <: Exception, B](either: Either[A, B]): Try[B] = {
    either match {
      case Right(obj) => Success(obj)
      case Left(err) => Failure(err)

    }
  }

  implicit def tryToEither[A](obj: Try[A]): \/[Throwable, A] = {
    obj match {
      case Success(something) => \/-(something)
      case Failure(err) => -\/(err)
    }
  }

  class RichOption[T](opt: Option[T]) {

    /**
      * Transform this Option to an equivalent Java Optional
      */
    def toOptional: Optional[T] = Optional.ofNullable(opt.getOrElse(null).asInstanceOf[T])
  }

  class RichOptional[T](opt: Optional[T]) {

    /**
      * Transform this Optional to an equivalent Scala Option
      */
    def toOption: Option[T] = if (opt.isPresent) Some(opt.get()) else None
  }

}

