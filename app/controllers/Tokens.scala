package controllers

import javax.inject.Inject

import actions.{TokenCheckAction, LoggingAction}
import dao.{TokenDao, UserDao}

import formats.APIJsonFormats
import models._
import dto._
import play.api.libs.json._
import play.api.mvc.{BodyParsers, Action, Controller}
import services.TokenService

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class Tokens @Inject()(userDao: UserDao, tokenService: TokenService, tokenCheckAction: TokenCheckAction, userController: Users) extends Controller with APIJsonFormats {


  def create = LoggingAction {
    Action.async(BodyParsers.parse.tolerantJson) { request =>
      tokenService.newToken(request.remoteAddress).map {
        token =>
          Created(Json.toJson(TopLevel(tokens = Some(token))))
      }
    }
  }

  def update(tokenId: String) = LoggingAction {
    tokenCheckAction.async(BodyParsers.parse.tolerantJson) { request =>
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
                userController.loginByEmail(email, passwordHash)(request.token)
              case LoginUser(None, Some(passwordHash), Some(username), None) =>
                userController.loginByUsername(username, passwordHash)(request.token)
              case LoginUser(None, None, None, Some(facebookToken)) =>
                userController.loginByFacebook(facebookToken)(request.token)
              case _ =>
                Future.successful(BadRequest(Error.toTopLevelJson(s"Missing fields to login")))
            }
          )
      }
    }
  }
}
