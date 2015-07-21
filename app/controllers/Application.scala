package controllers

import javax.inject.{Inject, Singleton}

import actions.{LoggingAction, TokenCheckAction}
import dao.StoryDao
import formats.APIJsonFormats
import models._
import play.api.libs.json.Json
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class Application @Inject()(storyDao: StoryDao, tokenCheckAction: TokenCheckAction) extends Controller with APIJsonFormats {

  def index = LoggingAction {
    tokenCheckAction { request =>
      val root = "https://" + request.host
      val services = Map("users" -> s"$root/users",
        "emails" -> s"$root/emails",
        "usernames" -> s"$root/usernames",
        "tokens" -> s"$root/tokens",
        "stories" -> s"$root/stories"
      )

      Ok(Json.toJson(TopLevel(links = Some(services))))
    }
  }

  def health = LoggingAction {
    Action.async {
      request =>
        storyDao.findRecommends(12).map {
          results =>
            val api = Map("api" -> Map("state" -> "active"))
            Ok(Json.toJson(api))
        }.recover {
          case _ =>
            InternalServerError(Error.toTopLevelJson(s"Database error: some functionalities could malfunction"))
        }
    }
  }
}
