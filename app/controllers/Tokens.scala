package controllers

import actions.{TokenCheckAction, LoggingAction}

import formats.APIJsonFormats
import models._
import play.api.libs.json._
import play.api.mvc.{BodyParsers, Action, Controller}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

object Tokens extends Controller with APIJsonFormats {

  // V1.0 compatibility
  def dispatcher = LoggingAction {
    Action.async(BodyParsers.parse.tolerantJson) { request =>
      (request.body \ "users") match {
        case _: JsUndefined =>
          createToken
        case obj: JsObject =>
          Token.newToken.flatMap {
            token =>
              update(token, obj)
          }
        case _ =>
          Future.successful(BadRequest(Error.toTopLevelJson(Error(s"You have provided a bad json"))))
      }
    }
  }

  def createDispatcher = LoggingAction {
    Action.async(BodyParsers.parse.tolerantJson) { request =>
      createToken
    }
  }

  def createToken = Token.newToken.map {
    token =>
      Created(Json.toJson(TopLevel(tokens = Some(token))))
  }

  // V1.0 compatibility
  def updateDispatcher(tokenId: String) = LoggingAction {
    TokenCheckAction.async(BodyParsers.parse.tolerantJson) { request =>
      request.token.userId match {
        case Some(_) =>
          Future.successful(BadRequest(Error.toTopLevelJson("An user is already logged on this token, discard this token and create a new one.")))
        case None =>
          update(request.token, (request.body \ "users"))
      }
    }
  }

  def update(token: Token, obj: JsValue) = {
    val userResult = obj.validate[LoginUser]
    userResult.fold(
      validationErrors => {
        Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
      },
      loginUser => loginUser match {
        case LoginUser(Some(email), Some(passwordHash), None, None) =>
          Users.loginByEmail(email, passwordHash)(token)
        case LoginUser(None, Some(passwordHash), Some(username), None) =>
          Users.loginByUsername(username, passwordHash)(token)
        case LoginUser(None, None, None, Some(facebookToken)) =>
          Users.loginByFacebook(facebookToken)(token)
        case _ =>
          Future.successful(BadRequest(Error.toTopLevelJson(s"You have nothing to specified to create a token")))
      }
    )
  }

}
