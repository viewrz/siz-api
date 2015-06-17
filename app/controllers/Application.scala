package controllers

import actions.{TokenCheckAction, LoggingAction}
import formats.APIJsonFormats
import models._
import play.api.libs.json.Json
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller with APIJsonFormats {

  def index = LoggingAction {
    TokenCheckAction { request =>
      val root = "https://"+request.host
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
        Story.findRecommends(12).map {
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
