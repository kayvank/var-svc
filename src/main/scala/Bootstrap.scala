import api.{ArtistApi, StatusApi, VideoApi}
import org.http4s.server.ServerApp
import org.http4s.server.blaze.BlazeBuilder

object Bootstrap extends ServerApp {
  import kamon.Kamon

  Kamon.start

  def server(args: List[String]) = BlazeBuilder.bindHttp(port=9000, host = "0.0.0.0")
    .mountService(VideoApi.wrappedService, "/video")
    .mountService(ArtistApi.wrappedService, "/artist")
    .mountService(StatusApi.wrappedService, "/status").start

}
