package controllers

import javax.inject.{Singleton, Inject}

import actions.{TokenCheckAction, LoggingAction}
import dao.{ViewerProfileDao, StoryDao, EventDao, UserDao}
import formats.APIJsonFormats
import models._
import play.api.libs.json.{Json, JsResult}
import play.api.mvc._
import services.EventService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class Events @Inject()(eventService: EventService, storyDao: StoryDao, tokenCheckAction: TokenCheckAction) extends Controller with APIJsonFormats {
  def manageValidationError[T](result: JsResult[T]): (T => Future[Result]) => Future[Result] = (action: T => Future[Result]) => result.fold(
    validationErrors => {
      Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
    }, action)

  def create = LoggingAction {
    tokenCheckAction.async(BodyParsers.parse.tolerantJson) { request =>
      val eventResult = (request.body \ "events").validate[NewEvent]

      manageValidationError[NewEvent](eventResult)({ newEvent: NewEvent =>
        storyDao.getById(newEvent.storyId).flatMap {
          case None =>
            Future.successful(NotFound(Error.toTopLevelJson(Error("No story for this id %s".format(newEvent.storyId)))))
          case Some(story) =>
            eventService.create(newEvent, story, request.token, request.remoteAddress).map {
              event =>
                Created(Json.toJson(TopLevel(events = Some(event))))
            }
        }
      })
    }
  }

}
