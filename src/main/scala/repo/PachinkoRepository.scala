package repo

import com.typesafe.scalalogging.LazyLogging
import model.Model._
import repo.cache.PachinkoCache
import utils.Global.cfgVevo
import scalaz.concurrent.Task

object PachinkoRepository extends LazyLogging {
  val api = new PachinkoCache(cfgVevo.getString("pachinko.api.url"))
  val relatedVideos: RecommendationRequest => Task[RecommendationResponse] = req => {

    api.pachinko(req).handleWith {
      case e: Exception =>
        logger.error(s"${e.getMessage}")
        Task.fail(e)
    }
  }
}
