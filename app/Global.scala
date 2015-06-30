import filters.CORSFilter
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import models._

import scala.concurrent.Future

object Global extends WithFilters(CORSFilter) with GlobalSettings {

  def updateDB = {
    User.updateDB
    Event.updateDB
    Story.updateDB
  }

  override def onStart(app: Application): Unit = {
    updateDB
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest(Error.toTopLevelJson(Error(error))))
  } 

  override def onError(request: RequestHeader, throwable: Throwable) = {
    Future.successful(InternalServerError(Error.toTopLevelJson(Error("Internal api error, try later"))))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(Error.toTopLevelJson(Error("This call don't exist, check the API documentation"))))
  }
}