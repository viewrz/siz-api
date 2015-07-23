package services

import javax.inject.{Inject, Singleton}

import dao.{EventDao, StoryDao}
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class StoryService @Inject()(eventDao: EventDao, storyDao: StoryDao) {
  def getBySlug(slug: String, token: Token, ip: String) = storyDao.getBySlug(slug).map { story =>
    (story, token.userId) match {
      case (Some(story), None) =>

        /** If the user is not connected, we must update his token and associate the video and ip to it, so that we may
          * push the same story when he uses the app. */
        eventDao.addEvent(
          Event(
            storyId = story.id,
            _type = "anonymous-view",
            tags = story.tags,
            viewerProfileId = token.viewerProfileId,
            ip = ip
          )
        )
      case _ =>
      // If the user is already connected we simply retrieve the story
    }
    story
  }
}
