package controllers

import formats.APIJsonFormats
import models.TopLevel
import play.api.libs.json.Json
import play.api.mvc._

object Application extends Controller with APIJsonFormats {

  def index = Action {
      val services = Map( "users"-> "/users",
                          "emails"-> "/emails",
                          "tokens"-> "/tokens",
                          "stories"-> "/stories"
      )
      Ok(Json.toJson(TopLevel(links=Some(services))))
    }

}
