package repo.cache

import com.google.common.cache.CacheBuilder
import io.circe.Json
import model.Model.RecommendationRequest
import org.http4s.{Method, Request, Uri}
import org.http4s.client.blaze.{defaultClient => client}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scalacache._
import guava._
import memoization._
import scalaz.concurrent.Task

final class TopsTrendingCache(urlString: String) {

  val underlyingGuavaCache =
    CacheBuilder.newBuilder().maximumSize(1500L).build[String, Object]

  implicit private val scalaCache =
    ScalaCache(GuavaCache(underlyingGuavaCache))

  import utils.CustomExecutor._
  import utils.FutureConverter._
  import model.ModelJsonProtocol._

  private def cached(country: String): Future[Json] = memoize(240 minutes) {
    val ttUrl =
      Uri.fromString(s"${urlString}?size=40&country=${country}").valueOr(throw _)
    client.expect[Json](Request(Method.GET, ttUrl)) runFuture()
  }

  def topsTrending(request: RecommendationRequest): Task[Json] = {
    cached(request.country) asTask
  }

}
