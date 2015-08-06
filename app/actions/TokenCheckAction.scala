package actions

import javax.inject.{Singleton, Inject}

import dao.TokenDao
import models.{User, Token, Error}
import play.api.Play
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current

import play.api.mvc.Results._

import scala.concurrent.Future

class TokenRequest[A](val token: Token, request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class TokenCheckAction @Inject() (tokenDao: TokenDao) extends ActionBuilder[TokenRequest] with ActionRefiner[Request, TokenRequest] {
  val access_token_header = Play.configuration.getString("api.accesstokenheader").get

  override def refine[A](request: Request[A]) =
    request.headers.get(access_token_header) match {
      case None =>
        Future.successful {
          Left(Unauthorized(Error.toTopLevelJson(Error(s"No token provided : use the Header '$access_token_header'"))))
        }
      case Some(access_token) =>
        tokenDao.findById(access_token).map {
          case token :: Nil if token.id == access_token =>
            Right(new TokenRequest(token, request))
          case _ =>
            Left(Unauthorized(Error.toTopLevelJson(Error(s"Unknown token $access_token"))))
        }
    }
}


