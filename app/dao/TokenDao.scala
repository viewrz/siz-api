package dao

import javax.inject.{Inject, Singleton}

import models.Token
import models.Token._
import play.api.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future
import scala.util.Random

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import formats.MongoJsonFormats._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class TokenDao @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends ReactiveMongoComponents {
  lazy val db = reactiveMongoApi.db

  def collection: JSONCollection = db.collection[JSONCollection]("tokens")

  def update(token: Token, userId: String): Future[Token] = {
    token.userId match {
      case None =>
        val newToken = new Token(token.id, userId, Some(userId))
        update(newToken).map(_ => newToken)
      case _ =>
        Logger.error("Try to update an already connected token")
        Future.successful(token)
    }
  }

  def create(token: Token) = collection.insert(token)

  def findById(id: String) = collection.find(Json.obj("_id" -> id)).cursor[Token]().collect[List]()

  def update(token: Token) = collection.update(Json.obj("_id" -> token.id), token)
}