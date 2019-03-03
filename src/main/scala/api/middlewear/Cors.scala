package api.middlewear

import org.http4s._
import headers.`Cache-Control`
import CacheDirective.`no-cache`
import util._

object  CorsMiddlewear {

  def apply(service: HttpService, header: Header): HttpService = Service.lift { request: Request =>
    service(request.putHeaders(header)).map( resp => 

      if (resp.status.isSuccess) resp.putHeaders(`Cache-Control`(NonEmptyList(`no-cache`())))
      else resp

    )
  }
}
