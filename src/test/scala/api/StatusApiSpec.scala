package api

import api.middlewear.CorsMiddlewear
import com.typesafe.scalalogging.LazyLogging
import org.http4s._
import org.http4s.server.middleware.{CORS, CORSConfig}
import org.specs2.mutable.Specification


class StatusApiSpec extends Specification with LazyLogging {
  "video api secifications".title
  lazy val corsConfig = CORSConfig(
    anyOrigin = true,
    anyMethod = false,
    allowedOrigins = Some(Set("*")),
    allowedMethods = Some(Set("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")),
    allowCredentials = true,
    allowedHeaders = Some(Set("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization")),
    maxAge = -1)

  "api supports cursor operations" >> {
    val service = StatusApi.apply()
    val corService = CORS(service, corsConfig)
    val wrappedService = CorsMiddlewear(corService, Header("Origin", "*"))
    val wrappedServiceWitCors = CORS(wrappedService, corsConfig)
    val request = new Request(method = Method.GET,
      uri = Uri(path = s""))
    val response = wrappedService.run(request).run
    logger.info(s"responseCors = ${response}")
    response.status === Status.Ok
  }
}
