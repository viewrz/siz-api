package controllers

import actions.{LoggingAction, TokenCheckAction}
import formats.APIJsonFormats

import models._
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json

import scala.concurrent.Future

object Stories extends Controller with APIJsonFormats {

  def dispatcher(limit: Int, orderBy: String, filterBy: String, slug: Option[String], sinceId: Option[String], lastSkippedId: Option[String]) = LoggingAction {
    TokenCheckAction.async { request =>
      slug match {
        case None =>
          find(limit, orderBy, request.token.viewerProfileId, request.token.userId != None, filterBy, sinceId, lastSkippedId)
        case Some(slug) =>
          getBySlug(slug)
      }
    }
  }

  def find(limit: Int, orderBy: String, viewerProfileId: String, tagsFilter: Boolean, filterBy: String, sinceId: Option[String], lastSkippedId: Option[String]) = {
    val removeTagsWithWeight = -3
    ViewerProfile.findById(viewerProfileId).flatMap {
      viewerProfile =>
        val filteredTags = tagsFilter match {
          case true =>
            viewerProfile.tagsFilterBy(_._2 <= removeTagsWithWeight)
          case false =>
            List()
        }
        filterBy match {
          case "recommends" =>
            val futureStories = Story.findRecommends(limit, orderBy,viewerProfile.likeStoryIds ::: viewerProfile.nopeStoryIds,filteredTags)
            futureStories.map {
              results =>
                Ok(Json.toJson(TopLevel(stories = Some(Right(results)))))
            }
          case "likes" =>
            val allIds = viewerProfile.likeStoryIds.reverse
            val ids = (lastSkippedId,sinceId) match {
              case (Some(lastSkippedId),None) =>
                allIds.dropWhile(_!=lastSkippedId).tail.take(limit)
              case (Some(lastSkippedId),Some(sinceId)) =>
                allIds.dropWhile(_!=lastSkippedId).tail.take(limit).takeWhile(_!=sinceId)
              case (None,Some(sinceId)) =>
                allIds.take(limit).takeWhile(_!=sinceId)
              case (None,None) =>
                allIds.take(limit)
              case _ =>
                List()
            }
            val futureStories =Story.getByIds(ids).map(_.sortBy(story => ids.indexOf(story.id)))
            val links: Option[Map[String,String]] = ids.headOption.map( _ => Map("previous" -> s"/stories?filterBy=likes&sinceId=%s".format(ids.head),
              "next" -> s"/stories?filterBy=likes&lastSkippedId=%s".format(ids.last)
            ) )
            futureStories.map {
              results =>
                Ok(Json.toJson(TopLevel(stories = Some(Right(results)), links = links)))
            }
          case _ =>
            Future.successful(BadRequest(Error.toTopLevelJson("Incorrect value for filterBy")))
        }

    }
  }

  def getById(id: String) = LoggingAction {
    TokenCheckAction.async { request =>
      Story.getById(id).map {
        case None =>
          NotFound(Error.toTopLevelJson(Error("No story for this id %s".format(id))))
        case Some(story) =>
          Ok(Json.toJson(TopLevel(stories = Some(Left(story)))))
      }
    }
  }

  def getBySlug(slug: String) = Story.getBySlug(slug).map {
      case None =>
        NotFound(Error.toTopLevelJson(Error("No story for this slug %s".format(slug))))
      case Some(story) =>
        Ok(Json.toJson(TopLevel(stories = Some(Left(story)))))
    }

}
