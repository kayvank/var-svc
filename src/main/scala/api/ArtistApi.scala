package api

import scalaz._, Scalaz._
import org.http4s.circe._
import org.http4s._
import org.http4s.dsl.{QueryParamDecoderMatcher, _}
import io.circe.syntax._
import scalaz.concurrent.Task
import scalaz.stream.Process

object ArtistApi extends BaseApi {
  def apply(): HttpService = service

  val service = HttpService {
    case request@GET -> Root / artistid / userid / country =>
      Ok(Map("message" -> (List(userid, country, artistid, s"page=1", s"size=20"))).asJson)
    case request@GET -> Root / artistid =>
      Ok(Map("message" -> (s"Hello again, ${artistid}")).asJson)
  }
}
