package api

import io.circe.Json
import io.circe.parser.parse
import scalaz._
import Scalaz._
import org.http4s._
import org.http4s.circe._
import io.circe.syntax._
import org.http4s.dsl._
import scala.util.Try
import scalaz.concurrent.Task

object StatusApi extends BaseApi {

  import utils.CustomExecutor._

  import repo.DS._

  val na = "na"
  val gocdPipelineCounter = Try(System.getProperty("build_number")).toOption.getOrElse(na)
  val bs = Map("gocdPipelineCounter" -> (
    (gocdPipelineCounter == null) ? na | gocdPipelineCounter)).asJson

  lazy final val bInfo = parse(info.BuildInfo.toJson).right.getOrElse(Json.Null)

  def apply(): HttpService = service


  val service = HttpService {
    case req@GET -> Root =>
      Ok(Task.fork(Task(bInfo)))

    case request@GET -> Root / "db" =>
      Ok(Task.fork(for {
        p <- Task(bInfo)
        r <- Task(p.deepMerge(Map("aurora-database-isUp" -> connectionStatus).asJson).deepMerge(bs))
      } yield (r))
      )
  }.mapK(Task.fork(_))

}
