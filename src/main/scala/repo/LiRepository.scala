package repo

import model.Model._
import scalaz.concurrent.Task
import scalaz._, Scalaz._

final object LiRepository {

  private final val api = new LiHelper(LiApiConfig())

  val relatedVideos: RecommendationRequest => Task[RecommendationResponse] = request => for {
    v0 <- api.liApi(request)
    v1 <- Task(dropPreviouslyShown(v0)(request))
  } yield (v1)

  val toDrop: Int => Int = itemCount =>
    (itemCount <= 0) ? PageSizePolicy().minSize | itemCount

  def dropPreviouslyShown(response: RecommendationResponse): RecommendationRequest => RecommendationResponse =
    recommendationReq => (recommendationReq.cursor.page == 1) ? response | response.copy(
      isrcs = response.isrcs.drop(toDrop(
        (recommendationReq.cursor.page - 1) * recommendationReq.cursor.size)))

}
