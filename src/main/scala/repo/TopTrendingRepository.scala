package repo

import io.circe.Json
import model.Model.{RecommendationRequest, RecommendationResponse}
import org.http4s.client.blaze.{defaultClient => client}
import com.typesafe.scalalogging.LazyLogging
import io.circe.optics.JsonPath._
import repo.cache.TopsTrendingCache
import utils.Global.cfgVevo

import scalaz.concurrent.Task

object TopTrendingRepository extends LazyLogging {

val api = new TopsTrendingCache(cfgVevo.getString("top_tranding.api.url"))

  def topTrending(json: Json): Task[RecommendationResponse] =
    Task(
      RecommendationResponse(
        isrcs = root.videos.each.isrc.string.getAll(json),
        provider = "topsAndTrending")
    )

  val topVideos: RecommendationRequest => Task[RecommendationResponse] = req => (for {
    v <- api.topsTrending(req)
    rvs <- topTrending(v)
  } yield (rvs)).handleWith {
    case e: Exception =>
      logger.error(s"${e.getMessage}")
      client.shutdown
      Task.fail(e)
  }

}
