package controllers

import actions.TokenCheckAction
import formats.APIJsonFormats

import models._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import scala.util.matching.Regex

import scala.concurrent.Future

object Stories extends Controller with APIJsonFormats {
  def dispatcher(limit: Int, orderBy: String, slug: Option[String]) = TokenCheckAction.async { request =>
    slug match {
      case None =>
        getAll(limit, orderBy)
      case Some(slug) =>
        getBySlug(slug)
    }
  }

  def getAll(limit: Int, orderBy: String) = {
      val futureStory = OldStory.getAll(limit,orderBy)
      futureStory.map{
        results =>
          Ok(Json.toJson(TopLevel(stories = Some(Right(Story.oldStoriesToStories(results))))))
      }
  }

  def getById(id: String) = TokenCheckAction.async { request =>
    val RegexId = "[0-9]{13}[0-9a-z]{11}".r
    id match {
      case RegexId() =>
          OldStory.getById(id).map {
            case None =>
              NotFound(Error.toTopLevelJson(Error("No story for this id %s and %s creationDate".format(id,OldStory.newIdToOldId(id)))))
            case Some(story) =>
              Ok(Json.toJson(TopLevel(stories = Some(Left(Story.oldStoryToStory(story))))))
          }
      case _ =>
        Future.successful(Ok(Error.toTopLevelJson(Error("Invalid id"))))
    }
  }

  def getBySlug(slug: String) = OldStory.getBySlug(slug).map {
      result =>
       Ok(Json.toJson(TopLevel(stories = Some(Left(Story.oldStoryToStory(result))))))
    }
}
