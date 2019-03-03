package repo.cache

import com.google.common.cache.CacheBuilder
import com.typesafe.scalalogging.LazyLogging
import model.Model.{RecommendationRequest, RecommendationResponse}
import org.http4s.{Method, Request, Uri}
import org.http4s.client.blaze.{defaultClient => client}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scalacache.ScalaCache
import scalacache.guava.GuavaCache
import scalacache.memoization.memoize
import scalaz.concurrent.Task

final class PachinkoCache(urlString: String) extends LazyLogging {

  val underlyingGuavaCache =
    CacheBuilder.newBuilder().maximumSize(30L).build[String, Object]

  implicit private val scalaCache =
    ScalaCache(GuavaCache(underlyingGuavaCache))

  import utils.CustomExecutor._
  import utils.FutureConverter._
  import model.ModelJsonProtocol._

  def pachinko(request: RecommendationRequest): Task[RecommendationResponse] =
    cached(request.assetId).asTask

  private def cached(isrc: String): Future[RecommendationResponse] = memoize(67 minutes) {
    val url = Uri.fromString(s"${urlString}${isrc}").valueOr(throw _)
    val relatedVideos: Task[List[String]] =
      client.expect[List[String]](
        Request(Method.GET, url))
    (for {
      r <- relatedVideos
      rvp = RecommendationResponse(
        isrcs = r,
        provider = "Pachinko")
    } yield (rvp)) runFuture()
  }
}
