package controllers

import actions.{LoggingAction, TokenCheckAction}

import akka.io.IO
import akka.util.Timeout
import akka.pattern.ask
import play.api.libs.concurrent.Akka
import spray.can.Http
import spracebook.SprayClientFacebookGraphApi
import spracebook.Exceptions._

import formats.APIJsonFormats
import models._
import play.api._
import play.api.libs.json.{JsValue, JsError, Json}
import play.api.mvc._
import play.api.Play.current

import reactivemongo.core.errors.DatabaseException
import utils.Hash

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.language.postfixOps

object Users extends Controller with APIJsonFormats {

  // V1.0 compatibility
  def createDispatcher = LoggingAction {
    Action.async(BodyParsers.parse.tolerantJson) { request =>
      request.headers.get(access_token_header) match {
        case None =>
          Token.newToken.flatMap {
            token =>
              createUser(token, request.body)
          }
        case Some(access_token) =>
          Token.findById(access_token).flatMap {
            case token :: Nil if token.id == access_token =>
              createUser(token, request.body)
            case _ =>
              Future.successful(Unauthorized(Error.toTopLevelJson(Error(s"Unknown token $access_token"))))
          }
      }
    }
  }

  def create = LoggingAction{
    TokenCheckAction.async(BodyParsers.parse.tolerantJson) { request =>
      request.token.userId match {
        case Some(_) =>
          Future.successful(BadRequest(Error.toTopLevelJson("An user is already logged on this token, discard this token and create a new one.")))
        case None =>
          createUser(request.token, (request.body \ "users"))
      }
    }
  }

  def createUser(token: Token, body: JsValue): Future[Result] = token.userId match {
    case Some(_) =>
      Future.successful(BadRequest(Error.toTopLevelJson("An user is already logged on this token, discard this token and create a new one.")))
    case None =>
      val userResult = (body \ "users").validate[NewUser]
      userResult.fold(
        validationErrors => {
          Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
        },
        user => user match {
          case NewUser(Some(email), Some(passwordHash), _, None) =>
            createUserByEmail(user)(token)
          case NewUser(_, None, _, Some(facebookToken)) =>
            createUserByFacebook(user, facebookToken)(token)
          case NewUser(None, Some(passwordHash), _, None) =>
            Future.successful(BadRequest(Error.toTopLevelJson(s"error with field /email : field missing")))
          case _ =>
            Logger.debug(s"Impossible to register ${user}")
            Future.successful(BadRequest(Error.toTopLevelJson(s"You have to specified password or facebookToken to create an user")))
        }
      )
  }

  def createUserByEmail(user: NewUser)(token: Token): Future[Result] = {
    val newUser = User.fromNewUser(user)
    createUser(newUser)(token)
  }

  def createUserByFacebook(user: NewUser, facebookToken: String)(token: Token): Future[Result] = {
    retrieveFacebookUserId(facebookToken).flatMap{
      facebookUserId =>
        val newUser = User.fromNewUser(user, Some(facebookUserId))
        createUser(newUser)(token)
    }.recover {
      case exception: FacebookException if exception.exceptionType == "OAuthException" =>
        Unauthorized(Error.toTopLevelJson(Error("Invalid facebook access token")))
      case InvalidAccessTokenException(_,_,_,_) =>
        Unauthorized(Error.toTopLevelJson(Error("Invalid facebook access token")))
      case AccessTokenExpiredException(_,_,_,_) =>
        Unauthorized(Error.toTopLevelJson(Error("Expired facebook access token")))
    }
  }

  def createUser(user: User)(token: Token): Future[Result] = {
    User.create(user).flatMap{ lastError =>
      Logger.debug(s"Successfully inserted with LastError: $lastError")
      Token.updateToken(token,user.id).map(
        token =>
          Created(Json.toJson(TopLevel(users = Some(user), tokens=Some(token))))
      )
    }.recover {
      case exception: DatabaseException if exception.code.contains(11000) && exception.getMessage().contains("emailUniqueIndex")=>
        Logger.debug("email already exist with database: "+exception.getMessage())
        Conflict(Error.toTopLevelJson(s"An user with email ${user.email} already exists"))
      case exception: DatabaseException if exception.code.contains(11000) && exception.getMessage().contains("usernameUniqueIndex")=>
        Logger.debug("username already exist with database: "+exception.getMessage())
        Conflict(Error.toTopLevelJson(s"An user with username ${user.username} already exists"))
      case exception: DatabaseException if exception.code.contains(11000) && exception.getMessage().contains("facebookUserIdUniqueIndex")=>
        Logger.debug("username already exist with database: "+exception.getMessage())
        Conflict(Error.toTopLevelJson(s"An user with facebookUserId ${user.facebookUserId.get} already exists"))
    }
  }

  def checkEmail(email: String) = LoggingAction{
      Action.async { request =>
        User.findByEmail(email).map {
          case User(Some(`email`), _, _, _, _, _, _, _) :: Nil =>
            Ok(Json.toJson(TopLevel(emails = Some(Left(Email(email, "registered"))))))
          case _ =>
            NotFound(Error.toTopLevelJson(Error("Email not found")))
        }
      }
  }


  def retrieveFacebookUserId(facebookToken: String): Future[String] = {
    implicit val system = Akka.system
    implicit val timeout = Timeout(10 seconds)

    val facebook: Future[SprayClientFacebookGraphApi] = for {
      Http.HostConnectorInfo(connector, _) <- IO(Http) ? Http.HostConnectorSetup("graph.facebook.com", 443, true)
    } yield {
      new SprayClientFacebookGraphApi(connector)
    }

    facebook.flatMap(_.getUser(facebookToken)).map {
      user =>
        Logger.debug(s"Retrieve user with id ${user.id} from facebook graph api")
        user.id
    }
  }


  def loginByEmail(email: String, loginPasswordHash: String)(token: Token): Future[Result] = User.findByEmail(email).flatMap {
    users => users match {
      case User(Some(`email`), Some(passwordHash), id, _, _,_, _, _) :: Nil if Hash.bcrypt_compare(loginPasswordHash,passwordHash) =>
        Token.updateToken(token,id).map(
          token =>
            Ok(Json.toJson(TopLevel(users = Some(users.head), tokens= Some(token))))
        )
      case User(Some(`email`), None, _, _, Some(_), Some(_), _, _) :: Nil =>
        Future.successful(Unauthorized(Error.toTopLevelJson(Error("User logged by facebook"))))
      case User(Some(`email`), _, _, _, _, _, _, _) :: Nil =>
        Future.successful(Unauthorized(Error.toTopLevelJson(Error("Incorrect password"))))
      case _ =>
        Future.successful(NotFound(Error.toTopLevelJson(Error("No user account for this email"))))
    }
  }

  def loginByUsername(username: String, loginPasswordHash: String)(token: Token): Future[Result] = User.findByUsername(username).flatMap {
    users => users match {
      case User(_, Some(passwordHash), id, Some(`username`), _, _, _, _) :: Nil if Hash.bcrypt_compare(loginPasswordHash,passwordHash) =>
        Token.updateToken(token,id).map(
          token =>
            Ok(Json.toJson(TopLevel(users = Some(users.head), tokens= Some(token))))
        )
      case User(_, None, _, Some(`username`), Some(_), Some(_), _, _) :: Nil =>
        Future.successful(Unauthorized(Error.toTopLevelJson(Error("User registered by facebook"))))
      case User(_, _, _, Some(`username`), _, _, _, _) :: Nil =>
        Future.successful(Unauthorized(Error.toTopLevelJson(Error("Incorrect password"))))
      case _ =>
        Future.successful(NotFound(Error.toTopLevelJson(Error("No user account for this username"))))
    }
  }


  def loginByFacebook(facebookToken: String)(token: Token): Future[Result] = {
    Users.retrieveFacebookUserId(facebookToken).flatMap{
      facebookUserId =>
        User.findByFacebookUserId(facebookUserId).flatMap {
          users => users match {
            case User(_, _, id, _, _, Some(`facebookUserId`), _, _) :: Nil =>
              Token.updateToken(token,id).map(
                token =>
                  Ok(Json.toJson(TopLevel(users = Some(users.head), tokens= Some(token))))
              )
            case _ =>
              Future.successful(NotFound(Error.toTopLevelJson(Error("No user account for the facebookUserId associated to this facebookToken"))))
          }
        }
    }.recover {
      case exception: FacebookException if exception.exceptionType == "OAuthException" =>
        Unauthorized(Error.toTopLevelJson(Error("Invalid facebook access token")))
      case InvalidAccessTokenException(_,_,_,_) =>
        Unauthorized(Error.toTopLevelJson(Error("Invalid facebook access token")))
      case AccessTokenExpiredException(_,_,_,_) =>
        Unauthorized(Error.toTopLevelJson(Error("Expired facebook access token")))
    }
  }

  val access_token_header = Play.configuration.getString("api.accesstokenheader").get

  def get(id: String) = LoggingAction {
      TokenCheckAction.async { request =>
        User.findById(id).map {
          case user :: Nil =>
            Ok(Json.toJson(TopLevel(users = Some(user))))
          case _ =>
            NotFound(Error.toTopLevelJson(Error(s"User $id not found")))
        }
      }
  }
}
