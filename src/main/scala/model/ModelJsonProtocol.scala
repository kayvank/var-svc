package model

import io.circe.generic.semiauto._
import io.circe._
import io.circe.syntax._
import Model._
import io.circe.optics.JsonPath.root
import cats.Monoid
import cats.syntax.semigroup._
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser.parse

object ModelJsonProtocol extends LazyLogging {


  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = org.http4s.circe.jsonOf[A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = org.http4s.circe.jsonEncoderOf[A]

  implicit val videoItemDecoder: Decoder[VideoItem] = deriveDecoder
  implicit val videoItemEncoder: Encoder[VideoItem] = deriveEncoder

  implicit val relatedVideoResponseDecoder: Decoder[RecommendationResponse] = deriveDecoder
  implicit val relatedVideoResponseEncoder: Encoder[RecommendationResponse] =
    deriveEncoder

  implicit val liVideoResponseDecoder: Decoder[LiRelatedVideoResponse] = deriveDecoder
  implicit val liVideoResponseEncoder: Encoder[LiRelatedVideoResponse] =
    deriveEncoder

  implicit val liVideoRequestDecoder: Decoder[LiVideoRequest] = deriveDecoder
  implicit val liVideoRequestEcoder: Encoder[LiVideoRequest] =
    deriveEncoder[LiVideoRequest].mapJsonObject(_.filter {
      case("userId", value) => !value.isNull
      case("anonId", value) => !value.isNull
      case _ => true
    })

  implicit val jsonMonoid = new Monoid[Json] {
    def empty = Json.Null

    def combine(j1: Json, j2: Json) = {
      j1.deepMerge(j2)
    }
  }

  def merge(items: List[Json]) =
    items.foldLeft(Monoid[Json].empty)(_ |+| _)

  val projectionIds: ApiV2Projection => Json = p => {
    val res = root.isrc.string
    res.getOption(p.json).get.asJson
  }
  val _projectionIds: (Json, String) => Json = (json, source) =>
    Map("provider" -> source).asJson.deepMerge(
      json.asObject.map(x => x.filterKeys(k => k == "isrc")).asJson)

  val projection: ApiV2Projection => Json = p => (
    for {
      a01 <- p.json.asObject.map(x => x.filterKeys(k => k == "monetizable"))
      a02 <- p.json.asObject.map(x => x.filterKeys(k => k == "isrc"))
      a03 <- p.json.asObject.map(x => x.filterKeys(k => k == "title"))
      a04 <- p.json.asObject.map(x => x.filterKeys(k => k == "urlSafeTitle"))
      a05 <- p.json.asObject.map(x => x.filterKeys(k => k == "year"))
      a06 <- p.json.asObject.map(x => x.filterKeys(k => k == "copyright"))
      a07 <- p.json.asObject.map(x => x.filterKeys(k => k == "shortUrl"))
      a08 <- p.json.asObject.map(x => x.filterKeys(k => k == "thumbnailUrl"))
      a09 <- p.json.asObject.map(x => x.filterKeys(k => k == "durationInMilliseconds"))
      a10 <- p.json.asObject.map(x => x.filterKeys(k => k == "hasLyrics"))
      a11 <- p.json.asObject.map(x => x.filterKeys(k => k == "isExplicit"))
      a12 <- p.json.asObject.map(x => x.filterKeys(k => k == "allowEmbed"))
      a13 <- p.json.asObject.map(x => x.filterKeys(k => k == "allowMobile"))
      a14 <- p.json.asObject.map(x => x.filterKeys(k => k == "isPremiere"))
      a15 <- p.json.asObject.map(x => x.filterKeys(k => k == "isUnlisted"))
      a16 <- p.json.asObject.map(x => x.filterKeys(k => k == "isLive"))
      a17 <- p.json.asObject.map(x => x.filterKeys(k => k == "isLift"))
      a18 <- p.json.asObject.map(x => x.filterKeys(k => k == "isOfficial"))
      a19 <- p.json.asObject.map(x => x.filterKeys(k => k == "isCertified"))
      a20 <- p.json.asObject.map(x => x.filterKeys(k => k == "isOriginalContent"))
      a21 <- p.json.asObject.map(x => x.filterKeys(k => k == "releaseDate"))
      a22 <- p.json.asObject.map(x => x.filterKeys(k => k == "views"))
      a23 <- p.json.asObject.map(x => x.filterKeys(k => k == "artists"))
      a24 <- p.json.asObject.map(x => x.filterKeys(k => k == "categories"))
      //      a25 <- p.json.asObject.map(x => x.filterKeys(k => k == "buyLinks"))
      a26 <- Map("isMonetizable" -> isMonizable(p.json, p.country)).asJson.asObject

    } yield (merge(List(Map("provider" -> p.recommendationSourc).asJson,
      a01.asJson, a02.asJson, a03.asJson, a04.asJson, a05.asJson,
      a06.asJson, a07.asJson, a08.asJson, todouble(a09.asJson), a10.asJson,
      a11.asJson, a12.asJson, a13.asJson, a14.asJson, a15.asJson,
      a16.asJson, a17.asJson, a18.asJson, a19.asJson, a20.asJson,
      a21.asJson, a22.asJson, a23.asJson, a24.asJson, a26.asJson //, a25.asJson
    )))).getOrElse(Json.Null)


  val todouble: Json => Json = jss => {
    val tx = root.durationInMilliseconds.number
    val d: Option[Double] = for {
      x <- tx.getOption(jss)
      y <- x.toLong
      z = y / 1000.00
    } yield (z)
    Map("duration" -> d.getOrElse(0.0)).asJson
  }

  def isViewable(j: Json, country: String): Boolean = {
    j.hcursor.downField("policy").get[List[String]]("viewable") match {
      case Right(countryList) => countryList.find(x => (x == "*") || (x == country)).isDefined
      case Left(e) => false
    }
  }

  val IsAssetExplicit: Boolean => Json => Boolean = flag => json => {
    val explicit = root.isExplicit.boolean.getOption(json).getOrElse(true)
    explicit == flag
  }

  val isViewableByCountry: String => Json => Boolean = country => json => {
    val v = root.policy.viewable.each.string.getAll(json)
    v.contains(country) || v.contains("*")
  }

  def isMonizable(j: Json, country: String): Boolean = {
    j.hcursor.downField("policy").get[List[String]]("monitiazble") match {
      case Right(countryList) => countryList.find(x => (x == "*") || (x == country)).isDefined
      case Left(e) => false
    }
  }

  def videoAsJson(videos: String): Json =
    parse(videos).fold(
      e => {
        logger.warn(s" Error parsing the recrod from video_data ${e.underlying.getMessage}. record = ${videos.headOption}")
        Json.Null
      },
      j => j)
}
