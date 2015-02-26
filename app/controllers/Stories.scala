package controllers

import actions.{LoggingAction, TokenCheckAction}
import formats.APIJsonFormats

import models._
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json

import scala.concurrent.Future

object Stories extends Controller with APIJsonFormats {

  def dispatcher(limit: Int, orderBy: String, slug: Option[String]) = LoggingAction {
    TokenCheckAction.async { request =>
      slug match {
        case None =>
          find(limit, orderBy, request.token.viewerProfileId, request.token.userId != None)
        case Some(slug) =>
          getBySlug(slug)
      }
    }
  }

  def find(limit: Int, orderBy: String, viewerProfileId: String, tagsFilter: Boolean) = {
    val removeTagsWithWeight = -3
    ViewerProfile.findById(viewerProfileId).flatMap {
      viewerProfile =>
        val filteredTags = tagsFilter match {
          case true =>
            viewerProfile.tagsFilterBy(_._2 <= removeTagsWithWeight)
          case false =>
            List()
        }
        val futureStories = Story.find(limit, orderBy,viewerProfile.likeStoryIds ::: viewerProfile.nopeStoryIds,filteredTags)
        futureStories.map {
          results =>
            Ok(Json.toJson(TopLevel(stories = Some(Right(results)))))
        }
    }
  }

  def getById(id: String) = LoggingAction {
    TokenCheckAction.async { request =>
      id match {
        case StoryIdRegex() =>
          Story.getById(id).map {
            case None =>
              NotFound(Error.toTopLevelJson(Error("No story for this id %s".format(id))))
            case Some(story) =>
              Ok(Json.toJson(TopLevel(stories = Some(Left(story)))))
          }
        case _ =>
          Future.successful(Ok(Error.toTopLevelJson(Error("Invalid id"))))
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
