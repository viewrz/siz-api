package http

import models.Error
import play.api.http.HttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc.{Result, RequestHeader}

import scala.Error
import scala.concurrent.Future
import play.api.http._
import play.api.http.Status._

/**
 * Created by fred on 16/07/15.
 */
class GlobalErrorHandler extends HttpErrorHandler {
  override def onClientError(request: RequestHeader,
                             statusCode: Int,
                             message: String):
  Future[Result] = {
    Status match {
      case BAD_REQUEST =>
        Future.successful(BadRequest(Error.toTopLevelJson(Error(message))))
      case NOT_FOUND =>
        Future.successful(NotFound(Error.toTopLevelJson(Error("This call does not exist, check the API documentation"))))
    }
  }

  override def onServerError(request: RequestHeader,
                             exception: Throwable):
  Future[Result] = Future.successful(InternalServerError(Error.toTopLevelJson(Error("Internal api error, try later"))))
}
