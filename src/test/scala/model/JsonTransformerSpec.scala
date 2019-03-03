package model

import org.specs2.mutable.Specification
import cats.syntax.either._
import io.circe.syntax._
import io.circe._
import io.circe.parser._
import com.typesafe.scalalogging.LazyLogging
import io.circe.optics.JsonPath._
import scala.io.Source
import model.Model.ApiV2Projection
import model.Model._
import repo.LiHelper

import scalaz._
import Scalaz._

class JsonTransformerSpec extends Specification with LazyLogging {
  "JsonTransformers specifications".title

  import ModelJsonProtocol._

  val jsonPayload: Json = parse(
    Source.fromInputStream(getClass.getResourceAsStream("/document.json")).mkString.stripMargin).getOrElse(Json.Null)

  "transform full video json object from json" >> {
    val computed = projection(ApiV2Projection(jsonPayload, "liftigniter", "US"))
    //    logger.info(s"projection test = ${computed}")
    computed.findAllByKey("views").headOption.isDefined
  }
  "conver list of jsons to Jsonarray" >> {
    val jList: List[Json] = List(Map(1 -> "1").asJson,
      Map(2 -> "2").asJson,
      Map(3 -> "3").asJson)
    val computed: Json = Json.fromValues(jList)
    logger.info(s"listJson to Json conversion : json=${computed.toString}")
    !computed.toString.isEmpty
  }

  "json conversion of li request" >> {
    val req = RecommendationRequest(
      assetId = "isrc-1",
      country = "us",
      userId = 123L.some,
      anonId = None,
      cursor = PageCursor(10, 2),
      token = null,
      isExplicit = None)

    val liRequest = new LiHelper().asLiRequest(req)
    val computedRequest = liRequest.asJson.as[LiVideoRequest].toOption

    computedRequest.isDefined &&
      computedRequest.get.userId == 123L.some &&
      computedRequest.get.maxCount == 20 &&
      computedRequest.get.showCount == 20
  }

  "For anonymous tokens, userId json tag should no be included in the Liftigniter request" >> {
    val liReq = LiVideoRequest(
      apiKey = "key1",
      currentItem = "hot-video",
      userId = None,
      anonId = 123L.some,
      maxCount = 10,
      showCount = 10,
      requestFields = List("id"),
      country = "US"
    )
    val _userId = root.userId.long
    val _anonoId = root.anonId.long
    val computedAnonId = _anonoId.getOption(liReq.asJson)
    val computedUserId = _userId.getOption(liReq.asJson)
    computedAnonId.isDefined && computedAnonId.get === 123L &&
    computedUserId.isEmpty
  }

    "For user tokens should not include anonymous json tag included in the Liftigniter Json request" >> {
    val liReq = LiVideoRequest(
      apiKey = "key1",
      currentItem = "hot-video",
      userId = 123L.some,
      anonId = None,
      maxCount = 10,
      showCount = 10,
      requestFields = List("id"),
      country = "US"
    )
    val _userId = root.userId.long
    val _anonoId = root.anonId.long
    val computedAnonId = _anonoId.getOption(liReq.asJson)
    val computedUserId = _userId.getOption(liReq.asJson)
    computedUserId.isDefined && computedUserId.get === 123L &&
      computedAnonId.isEmpty
  }
}
