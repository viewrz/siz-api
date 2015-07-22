package service

import javax.inject.Inject
import javax.inject.Singleton

import actions.TokenRequest
import dao.{StoryDao, TokenDao}
import models.{Story, Token, TopLevel, Error}

import scala.Error
import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class StoryService @Inject()(tokenDao: TokenDao, storyDao: StoryDao) {
  def getBySlug(slug: String, token: Token, ip: String) = storyDao.getBySlug(slug).map { story =>
    (story, token.userId) match {
      case (Some(story), None) =>

        /** If the user is not connected, we must update his token and associate the video and ip to it, so that we may
          * push the same story when he uses the app. */
        tokenDao.updateToken(
          token.copy(
            lastSeenIp = Some(ip),
            lastSeenStoryId = Some(story.id)))
      case _ =>
      // If the user is already connected we simply retrieve the story
    }
    story
  }
}
