package repo

import com.typesafe.scalalogging.LazyLogging

import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import cats.syntax.either._
import io.circe._
import io.circe.parser._

import scala.io.Source
import io.circe.syntax._
import model._
import Model._

import org.specs2.mutable.Specification

import scala.io.Source

class VideoRepositorySpec extends Specification with LazyLogging {
import VideoRepository._
  "Video Repository specifications".title
  "merge top vides and pachinco" >> {
    val r1 = Task(RecommendationResponse(List("v1", "v2", "v3"), "pachinco"))
    val r2 = Task(RecommendationResponse(List("v4", "v5", "v6"), "top-trending"))
    val tasks = concatRecommendations(Task.gatherUnordered(List(r1, r2)) )(5).run
    logger.info(s"---- merged tasks result = ${tasks}")
    tasks.isrcs.size === 5
  }
//
//  val json: Json = parse(
//    Source.fromInputStream(getClass.getResourceAsStream(
//      "/document.json")).mkString.stripMargin).getOrElse(Json.Null)
//
//  val jsonUSAOnly: Json = parse(
//    Source.fromInputStream(getClass.getResourceAsStream(
//      "/documentUSA.json")).mkString.stripMargin).getOrElse(Json.Null)
//
//  val jsonGBOnly: Json = parse(
//    Source.fromInputStream(getClass.getResourceAsStream(
//      "/documentGB.json")).mkString.stripMargin).getOrElse(Json.Null)
//
//  val mocRelatedVideoFunction: RelatedVideoRequest => Task[List[String]] = r =>
//    Task(List("GBUV71502150"))
//
//  val videoRepository = VideoRepository.apply()
//
//  "videos are viewable in allowd contries only" >> {
//    ModelJsonProtocol.isViewable(jsonUSAOnly, "USA") &&
//      ModelJsonProtocol.isViewable(jsonGBOnly, "GB") &&
//      ! ModelJsonProtocol.isViewable(jsonGBOnly, "USA") &&
//      ModelJsonProtocol.isViewable(json, "USA") &&
//      ModelJsonProtocol.isViewable(json, "GB")
//  }
//
//  "fetch video-isrc as json from vevo-db using repositoy asTAsk" >> {
//    val videoId = "GBUV71502150"
//    val computedJsonTask =
//      videoRepository.viewAbleVideo(ViewAbleVideoQuery(
//        isrc = videoId, country = "USA"))
//    val computedJson = computedJsonTask.run
//    //logger.info(s"computedJson = ${computedJson}")
//    computedJson != Json.Null
//  }
//
//  "fetch viewable vidoe" >> {
//    val videoId = "GBUV71502150"
//    val srcJson = Map("src" -> "liftigniter").asJson
//    val computedTask = videoRepository.viewAbleVideo(
//      ViewAbleVideoQuery(isrc = videoId, country = "USA"))
//    val computed = computedTask.run
//
//    !computed.asObject.get.fields.isEmpty
//  }
//
//  "get recommendations composition test using LI" >> {
//    val vRepo = VideoRepository.apply(LiftigniterSvc.apply().relatedVideos)
//    val computed = vRepo.relatedAssetTask(RelatedVideoRequest(
//      isrc = "USCJY1431509", country = "US", VideoCursor(page = 20, size = 10)
//    ), ModelJsonProtocol.projectionIds).run
//    //    logger.info(s"getRecommendation returned comupted=${computed}")
//    computed.isRight && computed.toOption.get.size > 1
//  }
}
