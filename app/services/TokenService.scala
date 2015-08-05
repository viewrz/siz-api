package services

import javax.inject.{Singleton, Inject}

import dao.{EventDao, TokenDao}
import models.Token
import play.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import reactivemongo.bson.BSONObjectID

import scala.util.Random

@Singleton
class TokenService @Inject()(tokenDao: TokenDao, eventDao: EventDao) {

  val TOKEN_ID_SIZE = 64

  private def generatedId = Random.alphanumeric.take(TOKEN_ID_SIZE).mkString

  def newToken(ip: String) = {
    val token = new Token(generatedId, BSONObjectID.generate.stringify)
    // we check if the same originating ip has already seen a particular video strip
    val newToken = eventDao.findLastOne(ip, "anonymous-view").map {
      case Some(event) =>
        Logger.info(s"this ip $ip has a token with a video ${event.storyId}")
        token.copy(storyIdToShow = Some(event.storyId))
      case _ =>
        Logger.info(s"this ip $ip has no anonymous-view recorded")
        token
    }
    newToken.flatMap{
      newToken =>
      tokenDao.create(newToken).map(_ => newToken)
    }
  }
}
