package controllers

import javax.inject.Inject

import actions.{TokenCheckAction, LoggingAction}
import dao.{TokenDao, UserDao}

import formats.APIJsonFormats
import models._
import play.api.libs.json._
import play.api.mvc.{BodyParsers, Action, Controller}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class Tokens @Inject()(userDao: UserDao, tokenDao: TokenDao, tokenCheckAction: TokenCheckAction, userController: Users) extends Controller with APIJsonFormats {

  // V1.0 compatibility
  def createDispatcher = LoggingAction {
    Action.async(BodyParsers.parse.tolerantJson) { request =>
      (request.body \ "users").toOption match {
        case None =>
          createToken
        case Some(obj: JsObject) =>
          tokenDao.newToken.flatMap {
            token =>
              update(token, obj)
          }
        case _ =>
          Future.successful(BadRequest(Error.toTopLevelJson(Error(s"You have provided a bad json"))))
      }
    }
  }

  /* v1.1 only
  def createDispatcher = LoggingAction {
    Action.async(BodyParsers.parse.tolerantJson) { request =>
      createToken
    }
  }*/

  def createToken = tokenDao.newToken.map {
    token =>
      Created(Json.toJson(TopLevel(tokens = Some(token))))
  }

  // V1.0 and V1.1
  def updateDispatcher(tokenId: String) = LoggingAction {
    tokenCheckAction.async(BodyParsers.parse.tolerantJson) { request =>
      (request.token.userId, (request.body \ "users").toOption) match {
        case (Some(_), _) =>
          Future.successful(BadRequest(Error.toTopLevelJson("An user is already logged on this token, discard this token and create a new one.")))
        case (None, Some(obj: JsObject)) =>
          update(request.token, obj)
        case _ =>
          Future.successful(BadRequest(Error.toTopLevelJson("'users field missing")))
      }
    }
  }

  def update(token: Token, obj: JsObject) = {
    val userResult = obj.validate[LoginUser]
    userResult.fold(
      validationErrors => {
        Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
      },
      loginUser => loginUser match {
        case LoginUser(Some(email), Some(passwordHash), None, None) =>
          userController.loginByEmail(email, passwordHash)(token)
        case LoginUser(None, Some(passwordHash), Some(username), None) =>
          userController.loginByUsername(username, passwordHash)(token)
        case LoginUser(None, None, None, Some(facebookToken)) =>
          userController.loginByFacebook(facebookToken)(token)
        case _ =>
          Future.successful(BadRequest(Error.toTopLevelJson(s"Missing fields to login")))
      }
    )
  }

}
