package api

import com.typesafe.scalalogging.LazyLogging
import org.specs2.mutable.Specification

import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import TokenHelper._
import model.Model
import model.Model.PageSizePolicy
import org.http4s.{Query, Request, Uri}

import scala.util._

class TokenHelperSpec extends Specification with LazyLogging {
  "token-helper specifications".title

  val token: String = "Ah6RxW9z5Suxiy8ttLmZDzN4_eHeyxf2SkXbH7XpCjg1.1498503600._TAUcStzRIsPXlziE9HUbfH1m8TimybRgADNmNZFdYDZ9VB3jdq-Na7LkuEoszwOa5LXGt8cunb1bKrp8YwT8TCltFQ1"

  "parse expired tokens to None successfully" >> {
    val uri = Uri(query = Query.fromPairs(("token", token)))
    val maybeToken = authToken(Request(uri = uri))
    Try(maybeToken.run).isFailure
  }

  "parse valid cursor form http request" >> {
    val uri = Uri(query = Query.fromPairs(("page", "1"), ("size", "7")))
    val computed = (cursor(Request(uri = uri))).run
    computed === Model.PageCursor(page = 1, size = 7)
  }

  "parse cursor with invalid size form http request" >> {
    val uri = Uri(query = Query.fromPairs(("page", "1"), ("size", (PageSizePolicy().maxSize + 1).toString)))
    Try(cursor(Request(uri = uri)).run).toOption.isEmpty
  }

  "parse cursor with invalid page form http request" >> {
    val uri = Uri(query = Query.fromPairs(("page", (PageSizePolicy().maxPage + 1).toString), ("size", "1")))
    Try(cursor(Request(uri = uri)).run).toOption.isEmpty
  }

}
