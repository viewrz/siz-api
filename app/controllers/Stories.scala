package controllers

import formats.APIJsonFormats

import models._
import play.api.Logger
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json

object Stories extends Controller with APIJsonFormats {
  def getAll(limit: Int, orderBy: String) = Action.async { request =>
      val futureStory = OldStory.getAll(limit,orderBy)
      futureStory.map{
        results =>
          Ok(Json.toJson(TopLevel(stories = Some(Story.oldStoriesToStories(results)))))
      }.recover {
        case exception: Exception =>
          Logger.error("Story error", exception)
          InternalServerError
      }
  }
}
