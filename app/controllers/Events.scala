package controllers

import actions.{TokenCheckAction, LoggingAction}
import formats.APIJsonFormats
import models.{ViewerProfile, Error, Event}
import play.api.libs.json.JsResult
import play.api.mvc._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Events extends Controller with APIJsonFormats {
  def manageValidationError[T](result: JsResult[T]): (T => Future[Result]) => Future[Result] = (action: T => Future[Result]) => result.fold(
    validationErrors => {
      Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
    },action)

  def create =  LoggingAction {
    TokenCheckAction.async(BodyParsers.parse.tolerantJson) { request =>
      val eventResult = (request.body \ "events").validate[Event]

      manageValidationError[Event](eventResult)({ event: Event =>
        ViewerProfile.addEvent(request.token.viewerProfileId,event).map(_ => NoContent)
      })
    }
  }
}
