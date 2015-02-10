package controllers

import actions.{TokenCheckAction, LoggingAction}
import formats.APIJsonFormats
import models._
import play.api.libs.json.JsResult
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Events extends Controller with APIJsonFormats {
  def manageValidationError[T](result: JsResult[T]): (T => Future[Result]) => Future[Result] = (action: T => Future[Result]) => result.fold(
    validationErrors => {
      Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
    },action)

  def create =  LoggingAction {
    TokenCheckAction.async(BodyParsers.parse.tolerantJson) { request =>
      val eventResult = (request.body \ "events").validate[NewEvent]

      manageValidationError[NewEvent](eventResult)({ newEvent: NewEvent =>
        val event = Event.newEventToEvent(newEvent, request.token.viewerProfileId)
        Event.addEvent(event).flatMap { _ =>
              ViewerProfile.processEvent(event).map(_ => NoContent)
        }
      })
    }
  }
}
