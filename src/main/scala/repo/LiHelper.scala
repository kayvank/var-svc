package repo

import com.typesafe.scalalogging.LazyLogging
import scalaz.concurrent.Task
import org.http4s.{Method, Request}
import org.http4s.client.blaze.{defaultClient => client}
import scalaz._
import Scalaz._
import model.Model._
import model.ModelJsonProtocol._

final class LiHelper(config: LiApiConfig=LiApiConfig()) extends LazyLogging{

  def asLiRequest(req: RecommendationRequest): LiVideoRequest =
    LiVideoRequest(
      apiKey = config.apiKey,
      currentItem = req.assetId,
      userId = req.userId,
      anonId = req.anonId,
      maxCount = computeMaxCount(req),
      showCount = req.cursor.page  * req.cursor.size,
      browsingHistory = req.previousAssetId.isDefined ? List(req.previousAssetId.get) | List(),
      country = req.country)

  def asLiResponse(req: LiVideoRequest): Task[LiRelatedVideoResponse] = {
    client.expect[LiRelatedVideoResponse](Request(Method.POST, config.url).withBody(req))

  }
  def asRecommendation(t: Task[LiRelatedVideoResponse]) = for {
    r <- t
    rvp <- Task(RecommendationResponse(
      isrcs = r.items.map(_.id), provider = "Liftigniter"))
  } yield (rvp)

  val computeMaxCount: RecommendationRequest => Int = req => {
  val sizeToRequest = req.cursor.size * req.cursor.page
    (req.isExplicit.isDefined && !req.isExplicit.get) ? (sizeToRequest+15) | sizeToRequest
  }

  val liApi =
    asLiRequest _ andThen
    asLiResponse _ andThen
    asRecommendation _
}

