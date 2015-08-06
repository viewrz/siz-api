package http

import models.Error
import play.Logger
import play.api.http.HttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc.{Result, RequestHeader}

import scala.concurrent.Future
import play.mvc.Http.Status._

class GlobalErrorHandler extends HttpErrorHandler {
  override def onClientError(request: RequestHeader,
                             statusCode: Int,
                             message: String):
  Future[Result] = {
    statusCode match {
      case BAD_REQUEST =>
        Future.successful(BadRequest(Error.toTopLevelJson(
          Error(message))))
      case NOT_FOUND =>
        Future.successful(NotFound(Error.toTopLevelJson(Error("This call does not exist, check the API documentation"))))
    }
  }

  override def onServerError(request: RequestHeader,
                             exception: Throwable):
  Future[Result] = {
    Logger.error("Server Error", exception)
    Future.successful(InternalServerError(Error.toTopLevelJson(Error("Internal api error, try later"))))
  }
}
