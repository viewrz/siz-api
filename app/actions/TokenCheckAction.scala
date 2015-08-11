package actions

import javax.inject.{Inject, Singleton}

import dao.TokenDao
import models.{Error, Token}
import org.slf4j.MDC
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

class TokenRequest[A](val token: Token, request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class TokenCheckAction @Inject()(tokenDao: TokenDao) extends ActionBuilder[TokenRequest] with ActionRefiner[Request, TokenRequest] {
  val access_token_header = Play.configuration.getString("api.accesstokenheader").get

  override def refine[A](request: Request[A]) =
    request.headers.get(access_token_header) match {
      case None =>
        Future.successful {
          Left(Unauthorized(Error.toTopLevelJson(Error(s"No token provided : use the Header '$access_token_header'"))))
        }
      case Some(access_token) =>
        tokenDao.findById(access_token).map {
          case token :: Nil if token.id == access_token => {
            MDC.put("token_id", token.id)
            token.userId.map(id => MDC.put("user_id", id.substring(0, 8)))
            Right(new TokenRequest(token, request))
          }
          case _ =>
            Left(Unauthorized(Error.toTopLevelJson(Error(s"Unknown token $access_token"))))
        }
    }
}


