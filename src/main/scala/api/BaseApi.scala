package api

import api.middlewear.CorsMiddlewear
import io.circe.{Decoder, Encoder, Json}
import org.http4s._
import org.http4s.server.middleware.{CORS, CORSConfig}

trait BaseApi {
  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = org.http4s.circe.jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = org.http4s.circe.jsonEncoderOf[A]

  val service: HttpService
  lazy val corsService = CORS(service, CORSConfig(
    anyOrigin = false,
    anyMethod = false,
    allowedOrigins = Some(Set("*")),
    allowedMethods = Some(Set("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")),
    allowCredentials = true,
    allowedHeaders = Some(Set("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization")),
    maxAge = 30))
  lazy val wrappedService = CorsMiddlewear(corsService, Header("Origin", "*"))
}
