package services

import javax.inject.{Inject, Singleton}

import dao.{EventDao, ViewerProfileDao}
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.Success

@Singleton
class EventService @Inject()(eventDao: EventDao, viewerProfileDao: ViewerProfileDao) {

  def create(newEvent: NewEvent, story: Story, token: Token, ip: String): Future[Event] = {
    val event = eventDao.newEventToEvent(newEvent, token.viewerProfileId, story.tags, ip)
    eventDao.addEvent(event).flatMap { _ =>
      viewerProfileDao.updateFromEvent(event).flatMap {
        case Success(false) =>
          viewerProfileDao.insert(ViewerProfile(
            event.viewerProfileId,
            if (event._type == "Like") List(event.storyId) else List(),
            if (event._type == "Nope") List(event.storyId) else List(),
            Some(event.tags.map(_ -> event.tagsWeight).toMap)
          )).map(_ => event)
        case Success(true) =>
          Future.successful(event)
      }
    }
  }
}
