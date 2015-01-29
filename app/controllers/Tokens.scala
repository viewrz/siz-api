package controllers

import actions.{TokenCheckAction, LoggingAction}

import formats.APIJsonFormats
import models._
import play.api.libs.json.Json
import play.api.mvc.{Result, BodyParsers, Action, Controller}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

object Tokens extends Controller with APIJsonFormats {

  def create= LoggingAction {
    Action.async(BodyParsers.parse.tolerantJson) { request =>
      Token.newToken.map {
        token =>
          Ok(Json.toJson(TopLevel(tokens = Some(token))))
      }
    }
  }

  def update(tokenId: String) = LoggingAction {
    TokenCheckAction.async(BodyParsers.parse.tolerantJson) { request =>
      request.token.userId match {
        case Some(_) =>
          Future.successful(BadRequest(Error.toTopLevelJson("An user is already logged on this token, discard this token and create a new one.")))
        case None =>
          val userResult = (request.body \ "users").validate[LoginUser]
          userResult.fold(
            validationErrors => {
              Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
            },
            loginUser => loginUser match {
              case LoginUser(Some(email), Some(passwordHash), None, None) =>
                Users.loginByEmail(email, passwordHash)(request.token)
              case LoginUser(None, Some(passwordHash), Some(username), None) =>
                Users.loginByUsername(username, passwordHash)(request.token)
              case LoginUser(None, None, None, Some(facebookToken)) =>
                Users.loginByFacebook(facebookToken)(request.token)
              case _ =>
                Future.successful(BadRequest(Error.toTopLevelJson(s"You have nothing to specified to create a token")))
            }
          )
      }
    }
  }


}
