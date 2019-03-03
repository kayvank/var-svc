package repo

import io.circe._
import DS._
import com.google.common.cache.CacheBuilder
import utils.Monitor.counterDbAcess
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scalacache._
import guava._
import memoization._
import scalaz.concurrent.Task


final class VideoCache {

  val underlyingGuavaCache =
    CacheBuilder.newBuilder().maximumSize(5000L).build[String, Object]

  implicit private val scalaCache =
    ScalaCache(GuavaCache(underlyingGuavaCache))

  import utils.CustomExecutor._
  import utils.FutureConverter._
  import model.ModelJsonProtocol._

  final def findVideoRecord(isrc: String): Task[Json] =
    cached(isrc) asTask

  private def cached(isrc: String): Future[Json] =
    memoize(71 minutes) {
      (for {
        t <- findVideoByIsrc(isrc)(hxa)
        _ = Task.now(counterDbAcess.increment())
        json <- Task(t map videoAsJson)
      } yield (json)) runFuture() map (_.getOrElse(Json.Null))
    }
}
