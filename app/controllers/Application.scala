package controllers

import actions.{TokenCheckAction, LoggingAction}
import formats.APIJsonFormats
import models.TopLevel
import play.api.libs.json.Json
import play.api.mvc._

object Application extends Controller with APIJsonFormats {

  def index = LoggingAction {
    TokenCheckAction { request =>
      val root = "https://"+request.host
      val services = Map("users" -> s"$root/users",
        "emails" -> s"$root/emails",
        "tokens" -> s"$root/tokens",
        "stories" -> s"$root/stories"
      )
      
      Ok(Json.toJson(TopLevel(links = Some(services))))
    }
  }
}
