package api

import com.typesafe.scalalogging.LazyLogging
import model.Exceptions.InvalidVideoId
import model.ModelJsonProtocol
import scalaz._, Scalaz._
import repo._
import org.http4s._
import org.http4s.dsl._
import utils.Global
import org.http4s.circe._
import scalaz.concurrent.Task
import utils.Monitor._
import Ex._

object VideoApi extends BaseApi with LazyLogging {

  import utils.CustomExecutor._

  val apiVersion = Global.cfgVevo.getString("api.version")

  def apply(): HttpService = service

  val videoRepository =
    VideoRepository()

  val service = HttpService {

    case req@OPTIONS -> Root / isrc / "related" => {
      Ok()
    } handleWith { case e => throwbe2response(e) }

    case req@GET -> Root / isrc / "related" => {
      Ok(Task.fork(for {
        v1 <- TokenHelper.httpRequestParameters(req, isrc)
        vid <- validateIsrc(isrc)
        v2 <- videoRepository.recommendations(v1, ModelJsonProtocol.projection)
        _ <- Task(counterRelated200s.increment())

      } yield (v2)))
    }.handleWith { case e => throwbe2response(e) }

    case req@OPTIONS -> Root / apiVersion / "ids" / isrc => {
      Ok()
    } handleWith { case e => throwbe2response(e) }

    case req@GET -> Root / apiVersion / "ids" / isrc => {
      val _ = counterHttpReq.increment()
      val vidRequest = Task.fork(TokenHelper.httpRequestParameters(req, isrc))
      val validatedIsrc = Task.fork(validateIsrc(isrc))
      Ok(Task.fork(for {
        v1 <- vidRequest
        vid <- validatedIsrc
        v2 <- videoRepository.recommendations(v1)
        _ <- Task(counterByIsrc200s.increment())
      } yield (v2)))
    }.handleWith { case e => throwbe2response(e) }

    case req@_ =>
      val _ = counter400s.increment()
      NotFound()
  }.mapK(Task.fork(_))

  def optionalHeader(req: Request): String => Option[String] = { key =>
    req.headers.find(p => p.name.toString == key).map(_.name.toString)
  }

  def validateIsrc(isrc: String) = {
    val isrcRegx = """^[A-Z]{2}[A-Z0-9]{3}[0-9]{2}[0-9]{5}$""".r
    val tvRegx = """TIVEV[0-9]{2}[0-9]{5}""".r
    val tvChannelRegx = """TIVEVSTR[A-Za-z]{2}[0-9]{2}""".r
    val slateRegx = """TIVEVSLATE[0-9]{2}""".r
    if (
      isrcRegx.findFirstIn(isrc).isDefined ||
        tvRegx.findFirstIn(isrc).isDefined ||
        tvChannelRegx.findFirstIn(isrc).isDefined ||
        slateRegx.findFirstIn(isrc).isDefined
    ) Task(isrc)
    else Task.fail(new InvalidVideoId(s"video-id: ${isrc} is invalid!"))
  }

}
