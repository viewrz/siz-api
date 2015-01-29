package models

import play.api.Logger
import play.api.libs.json.Json
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scala.util.Random

case class Token(id: String, userId: Option[String]= None)

object Token extends MongoModel("tokens") {
  val TOKEN_ID_SIZE = 64
  private def generatedId = Random.alphanumeric.take(TOKEN_ID_SIZE).mkString
  def newToken = {
    val token = new Token(Token.generatedId)
    val futureToken = create(token)
    futureToken.map(_ => token)
  }
  def updateToken(token: Token, userId: String): Future[Token] ={
    token.userId match {
      case None =>
        val newToken = new Token(token.id, Some(userId))
        updateToken(newToken).map(_ => newToken)
      case _ =>
        Logger.error("Try to update an already connected token")
        Future.successful(token)
    }
  }

  def create(token: Token) = collection.insert(token)
  def findById(id: String) = collection.find(Json.obj("_id" -> id)).cursor[Token].collect[List]()
  def updateToken(token: Token) = collection.update(Json.obj("_id" -> token.id),token)
}