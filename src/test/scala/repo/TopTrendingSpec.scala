//package repo
//
//import com.typesafe.scalalogging.LazyLogging
//import com.vevo.tokens.{ReadOnlyToken, Token}
//import io.circe.Json
//import io.circe.parser.parse
//import model.Model.{AssetRequest, ProductCursor, RelatedVideoResponse}
//import org.specs2.mutable.Specification
//import cats.syntax.either._
//import scalaz._, Scalaz._
//import scala.io.Source
//import io.circe._
//import io.circe.parser._
//import io.circe.optics.JsonPath._
//
//import scalaz.concurrent.Task
//
//class TopTrendingSpec extends Specification with LazyLogging {
//
//  "top trending specs".title
//
//  import repo.TopTrendingRepository._
//
//  val jsonPayload: Json = parse(
//    Source.fromInputStream(getClass.getResourceAsStream("/topTrending.json")).mkString.stripMargin).getOrElse(Json.Null)
//
////  "top trending should fetch vidoes" >> {
////    val request = AssetRequest("1234", "us", "anonymous", ProductCursor(), ReadOnlyToken.builder().build())
////    val jsonObj = getTT(request).run
////    logger.debug(s"TT returned -> ${jsonObj}")
////    !jsonObj.toString().isEmpty
////  }
//  "get top tending videoids" >> {
//    val computed: RelatedVideoResponse = topTrending(jsonPayload).run
//    logger.debug(s"isrc from static tt file -> ${computed}")
//    !computed.isrcs.isEmpty
//  }
//
//}
