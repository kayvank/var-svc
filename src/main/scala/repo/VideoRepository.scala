package repo

import java.util.concurrent.TimeoutException
import scalaz.concurrent.Task
import scalaz._, Scalaz._
import com.typesafe.scalalogging.LazyLogging
import model._
import ModelJsonProtocol._
import scala.concurrent.duration.{Duration, _}
import io.circe.Json
import io.circe.syntax._
import Model._
import utils._


object VideoRepository {
  def apply(
             liRelatedVideos: RecommendationRequest => Task[RecommendationResponse] = LiRepository.relatedVideos,
             pKRelatedVideos: RecommendationRequest => Task[RecommendationResponse] = PachinkoRepository.relatedVideos,
             topRelatedVideos: RecommendationRequest => Task[RecommendationResponse] = TopTrendingRepository.topVideos,
             videoCache: VideoCache = new VideoCache()
           ) = new VideoRepository(liRelatedVideos, pKRelatedVideos, topRelatedVideos, videoCache)

  def concatRecommendations(tasks: Task[List[RecommendationResponse]]): Int => Task[RecommendationResponse] = size =>
    Task.fork(for {
      ts <- tasks
      d = ts.foldLeft(RecommendationResponse(List[String](), ""))((a, c) =>
        RecommendationResponse((a.isrcs ++ c.isrcs), "pachinco + topTrending"))
    } yield (d.copy(d.isrcs.take(size))))

  val isExplicitFilter: IsExplicitWithId => Json => Json = query => json =>
    query.isExplicit match {
      case Some(f) if f => json // give me all
      case Some(f) if (!f && IsAssetExplicit(f)(json)) => json
      case None => json
      case _ => Json.Null
    }

  def fallback(request: RecommendationRequest): Task[Json] =
    for {
      t <- TopTrendingRepository.topVideos(request)
      g <- Task(t.asJson)
    } yield (g)
}

final class VideoRepository(
                             liVr: RecommendationRequest => Task[RecommendationResponse],
                             pkVr: RecommendationRequest => Task[RecommendationResponse],
                             topvideos: RecommendationRequest => Task[RecommendationResponse],
                             videoCache: VideoCache
                           ) extends LazyLogging {

  import Global._
  import Monitor._
  import VideoRepository._

  final val defaultTimeout: Duration =
    cfgVevo.getLong("client.maxresponse.time.millisecond") milliseconds

  val viewAbleRecommendationList: List[IsExplicitWithId] => Task[List[Json]] = videos => Task.fork(
    for {
      x <- Nondeterminism[Task].gatherUnordered((videos.toParArray map viewAbleRecommendation).toList)
      z <- Task(x.filter(cc => cc != Json.Null))
    } yield (z))

  val viewAbleRecommendation: IsExplicitWithId => Task[Json] = query => Task.fork(
    for {
      v0 <- videoCache.findVideoRecord(query.isrc)
      v2 <- Task(isExplicitFilter(query)(v0))
    } yield (v2))

  def viewableRecommendations(
                               relatedVideo: RecommendationResponse,
                               assetRequest: RecommendationRequest): Task[List[Json]] =
    viewAbleRecommendationList(relatedVideo.isrcs map (
      IsExplicitWithId(_, assetRequest.isExplicit))) //TODO use composition

  def getRelatedAssets(
                        recommendationResp: Task[RecommendationResponse],
                        recommendationReq: RecommendationRequest,
                        projection: ApiV2Projection => Json): Task[List[Json]] = Task.fork(
    for {
      v0 <- recommendationResp
      v1 <- viewableRecommendations(v0, recommendationReq)
      jsonVideosProjection <- Task(v1 map (j =>
        projection(ApiV2Projection(j, v0.provider, recommendationReq.country))))
      jsonV <- Task(
        (jsonVideosProjection.isEmpty) ? (throw new
            NoSuchElementException(s"video-id: ${recommendationReq.assetId} has no related videos")
          ) | jsonVideosProjection)
    } yield (jsonV))

  def askProvidersForRecommendations(request: RecommendationRequest): Task[RecommendationResponse] = {
    val liftigniter = Task.fork(liVr(request).timed(defaultTimeout))
    val pachinko = Task.fork(pkVr(request))
    val topV = Task.fork(topvideos(request))
    liftigniter handleWith {
      case e: TimeoutException =>
        val _ = counterNoneLiftigniter.increment()
        logger.warn(s"liftigniter timedout after ${defaultTimeout.toMillis} millis--Defaulting to Pachinko")
        concatRecommendations(Task.gatherUnordered(List(pachinko, topV)))(request.cursor.size)
      case e: Throwable =>
        val _ = counterInternalTimeout
        logger.warn(s"---received exception: ${e.getMessage}")
        e.printStackTrace()
        concatRecommendations(Task.gatherUnordered(List(pachinko, topV)))(request.cursor.size)
    }
  }

  def recommendations(request: RecommendationRequest,
                      projection: ApiV2Projection => Json): Task[Json] = Task.fork(
    for {
      j <- getRelatedAssets(
        askProvidersForRecommendations(request), request, projection)
      j2 <- Task(Json.fromValues(j.take(request.cursor.size)))
    } yield (j2))

  def recommendations(request: RecommendationRequest): Task[Json] =
    (for {
      v <- askProvidersForRecommendations(request)
      g <- Task(v.copy(isrcs = v.isrcs.take(request.cursor.size)).asJson)
    } yield (g)).handleWith {
      case e: Exception => {
        logger.error(s"Exception in filteredBestResponse routine: ${e.getMessage}")
        for {
          b <- topvideos(request)
          j <- Task(b.asJson)
        } yield j
      }
    }
}
