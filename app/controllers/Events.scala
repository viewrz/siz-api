package controllers

import javax.inject.Inject

import actions.{TokenCheckAction, LoggingAction}
import controllers.Stories._
import dao.{StoryDao, EventDao, UserDao}
import formats.APIJsonFormats
import models._
import play.api.libs.json.{Json, JsResult}
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Events @Inject()(storyDao: StoryDao, eventDao: EventDao) extends Controller with APIJsonFormats {
  def manageValidationError[T](result: JsResult[T]): (T => Future[Result]) => Future[Result] = (action: T => Future[Result]) => result.fold(
    validationErrors => {
      Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
    }, action)

  def create = LoggingAction {
    TokenCheckAction.async(BodyParsers.parse.tolerantJson) { request =>
      val eventResult = (request.body \ "events").validate[NewEvent]

      manageValidationError[NewEvent](eventResult)({ newEvent: NewEvent =>
        storyDao.getById(newEvent.storyId).flatMap {
          case None =>
            Future.successful(NotFound(Error.toTopLevelJson(Error("No story for this id %s".format(newEvent.storyId)))))
          case Some(story) =>
            val event = eventDao.newEventToEvent(newEvent, request.token.viewerProfileId, story.tags)
            eventDao.addEvent(event).flatMap { _ =>
              ViewerProfile.processEvent(event).map(_ => Created(Json.toJson(TopLevel(events = Some(event)))))
            }
        }
      })
    }
  }
}
