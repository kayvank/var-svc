package api

import javax.security.sasl.AuthenticationException
import com.vevo.tokens.Token
import com.vevo.tokens.UserId
import scala.util.{Failure, Success, Try}
import com.typesafe.scalalogging.LazyLogging
import model.Model._
import model.Exceptions._
import org.http4s.Request
import org.http4s.util._
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import scala.collection.JavaConversions._
import utils.JavaOptionals._
import utils.TokenWrapper._
import utils.FailedComputationWrapper._
import utils.Global._

object TokenHelper extends LazyLogging {

  final val cursorLimit = PageSizePolicy()
  final val registered = "registered"
  final val defaultSize = 20
  final val defaultPage = 1
  final val previous_isrc = "previous_isrc" //TODO verify http param with web team
  final val userOverrideHeader = CaseInsensitive( cfgVevo.getString("api.user.id.override.header") )
  final val userOverrideValue = CaseInsensitive(cfgVevo.getString("api.user.id.override.value") )

  def country(token: Token, req: Request): Task[String] = {
    lazy val countryFromHeader = req.headers.find({
      p =>
        val c = p.name.toString.toLowerCase
        c == "country" || c == "country-code"
    }).map(_.name.toString)
    val countryFromToken = token.getCountry.toOption
    (countryFromToken orElse countryFromHeader) match {
      case None => Task.fail(CountryComputationException())
      case Some(country) => Task.now(country)
    }
  }
//TODO verify with webtem to use previ
  def previousIsrc(req: Request): Option[String]  =
    req.uri.query.params.find(_._1.toLowerCase == previous_isrc).map(_._2)

  def cursor(req: Request): Task[PageCursor] = {
    val page = (req.uri.query.params.find(
      _._1.toLowerCase == "page").flatMap(
      x => Try(x._2.toInt).toOption)) getOrElse (defaultPage)

    val size = (req.uri.query.params.find(
      _._1.toLowerCase == "size").flatMap(
      x => Try(x._2.toInt).toOption)) getOrElse (defaultSize)

    if ((1 to cursorLimit.maxPage).contains(page) &&
      (1 to cursorLimit.maxSize).contains(size))
      Task.now(PageCursor(size = size, page = page))
    else
      Task.fail(RequestMaxSizeException(
        s"size violation. [pagesize=${page} size=${size}] are not in the range of page:[1 .. ${PageSizePolicy().maxPage}] size:[1 ..${PageSizePolicy().maxSize}]!"))
  }

  def authToken(req: Request): Task[Token] = {
    val urlToken: Option[String] = req.uri.query.params.find(_._1.toLowerCase == "token").map(_._2)
    lazy val headerToken: Option[String] = for {
      a1 <- req.headers.find(p => p.name == CaseInsensitiveString("authorization"))
      a3 <- a1.value.split("\\s").tail.headOption
    } yield (a3)

    (urlToken orElse (headerToken)) match {
      case None => Task.fail(new AuthenticationException("No token found in header or uri"))
      case Some(authToken) => Task(tokenKey.parseToken(authToken))
    }
  }

  def userId(token: Token): Task[Option[Long]] = Try(token.getSubject) match {
    case Success(userId) => Task.now(userId.toOption.map(x => UserId.toLong(x)))
    case Failure(e) => Task.fail(UserComputationException(e.getMessage))
  }

  def httpRequestParameters(req: Request, isrc: String): Task[RecommendationRequest] = for {
    _token <-  authToken(req)
    _country <- country(_token, req)
    _cursor <- cursor(req)
    _userId <- userId(_token)
    _anonId <- Task(_token.getRoles.map(_.toString.toLowerCase).find(_ == registered).isEmpty ? _userId | None)
    _isExplicit <- Task(req.uri.query.params.find(_._1.toLowerCase == "isexplicit"
    ).flatMap(x => Try(x._2.toBoolean).toOption))
  } yield {
overrideUser( req,
  RecommendationRequest(
    assetId = isrc,
    previousAssetId = previousIsrc(req),
    country = _country,
    userId = _anonId.isEmpty ? _userId | None,
    anonId = _anonId,
    cursor = _cursor,
    token = _token,
    isExplicit = _isExplicit)
)}
  def overrideUser(
    req: Request,
    recReq: RecommendationRequest): RecommendationRequest =
    if ( req.headers.exists(p => CaseInsensitiveString(p.name.toString) == (userOverrideHeader)) )
      recReq
    else
      recReq.copy(userId=None, anonId=None)

}
