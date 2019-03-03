package repo

import com.typesafe.scalalogging.LazyLogging
import org.specs2.mutable.Specification
import doobie.imports._

import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import io.circe.parser._
import io.circe.syntax._

class DSSpec extends Specification with LazyLogging {

  import DS._

  "Data srouce specifications".title

  "connect to databse and run queries" >> {
    42.point[ConnectionIO].transact(hxa).run === 42

  }
  //
  //  "fetch video-data from vevo-db" >> {
  //    val computed = queryByIsrc("GBUV71502150")
  //    val _computedTask: Task[Option[String]] =
  //      queryByIsrc("GBUV71502150").transact(hxa)
  //    val computedTask: Task[Either[io.circe.ParsingFailure, io.circe.Json]] =
  //      computed.transact(hxa) map (t => parse(t.head))
  //    val res: Either[io.circe.ParsingFailure, io.circe.Json] = computedTask.run
  //    res.isRight
  //  }
  //
  //  "valid isrc exist" >> {
  //    val computedTask: Task[Option[Int]] = queryIsrcExists("GBUV71502150").transact(hxa)
  //    val computed = computedTask.run
  //    println(s"exists printed ++++++++++ ${computed}")
  //    computed.isDefined && computed.get > 0
  //  }
  //
  //  "invalid isrc does not exist" >> {
  //    val computedTask: Task[Option[Int]] = queryIsrcExists("ZBUV71502150").transact(hxa)
  //    val computed = computedTask.run
  //    println(s"exists printed ++++++++++ ${computed}")
  //    computed.isDefined && computed.get == 0
  //  }

}
