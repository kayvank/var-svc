package model

import com.vevo.tokens.Token
import io.circe.Json
import utils.Global._
import org.http4s._

object Model {

  sealed class Model

  case class VideoItem(id: String)
    extends Model

  case class RecommendationResponse(
    isrcs: List[String],
    provider: String)
    extends Model

  case class LiRelatedVideoResponse(
   items: List[VideoItem])
    extends Model

  case class IsExplicitWithId(
    isrc: String,
    isExplicit: Option[Boolean] = None)
    extends Model

  case class LiVideoRequest(
    apiKey: String,
    currentItem: String,
    userId: Option[Long],
    anonId: Option[Long],
    maxCount: Int,
    showCount: Int,
    requestFields: List[String] = List("id"),
    country: String,
    browsingHistory: List[String]=List(),
    timestamp: Long = System.currentTimeMillis
  ) extends Model

  case class LiApiConfig(
    url: Uri = Uri.fromString(cfgVevo.getString("liftigniter.api.url")).valueOr(throw _),
    apiKey: String = cfgVevo.getString("liftigniter.api.key")
  ) extends Model

  case class PageCursor(size: Int = 20, page: Int = 1
  ) extends Model

  case class ApiV2Projection(
    json: Json,
    recommendationSourc: String,
    country: String)
    extends Model

  case class RecommendationRequest(
    assetId: String,
    previousAssetId: Option[String]=None,
    country: String,
    userId: Option[Long],
    anonId: Option[Long],
    cursor: PageCursor,
    token: Token,
    isExplicit: Option[Boolean] = None
  ) extends Model

  case class PageSizePolicy(
    maxPage: Int = cfgVevo.getInt("items.returned.max.page"),
    maxSize: Int = cfgVevo.getInt("items.returned.max.size"),
    minSize: Int = cfgVevo.getInt("items.returned.min.size")
  ) extends Model
}
