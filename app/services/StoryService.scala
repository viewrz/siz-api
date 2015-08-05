package services

import javax.inject.{Inject, Singleton}

import dao.{TokenDao, EventDao, StoryDao}
import models._
import play.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class StoryService @Inject()(eventDao: EventDao, storyDao: StoryDao, tokenDao: TokenDao) {
  private val removeTagsWithWeight = -5

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

  def getById(id: String, token: Token) = storyDao.getById(id).map { story =>
    token.storyIdToShow match {
      case Some(storyIdToShow) if storyIdToShow == id =>
        // Si le token fourni correspond à la story en cours de recup, on met à jour le token pour éviter que
        // le visiteur aie de nouveau la story pushée à lui.
        Logger.info(s"Found story whose id is matching the token storyIdToShow. story is ${token.storyIdToShow}")
        tokenDao.update(token.copy(storyIdToShow = None))
      case _ =>
    }
    story
  }

  def findRecommends(limit: Int, orderBy: String, viewerProfile: ViewerProfile, token: Token) = {
    val filteredTags = token.userId.isDefined match {
      case true =>
        viewerProfile.tagsFilterBy(_._2 <= removeTagsWithWeight)
      case false =>
        List()
    }
    val exceptStoryIds = viewerProfile.likeStoryIds ::: viewerProfile.nopeStoryIds
    val futureStories = storyDao.findRecommends(limit, orderBy, exceptStoryIds, filteredTags)
    token.storyIdToShow match {
      case Some(storyId) =>
        // If we have a storyIdToShow, inject this story in first place and remove it from the token
        tokenDao.update(token.copy(storyIdToShow = None))
        Logger.info(s"Push story in recommends. story is ${token.storyIdToShow}")
        storyDao.getById(storyId).flatMap {
          case Some(story) =>
            futureStories.map(story :: _)
          case None =>
            futureStories
        }
      case None =>
        futureStories
    }
  }
}
