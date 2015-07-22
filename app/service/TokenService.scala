package service

import javax.inject.Inject

import dao.{EventDao, TokenDao}
import models.Token
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import reactivemongo.bson.BSONObjectID

import scala.util.Random

class TokenService @Inject()(tokenDao: TokenDao, eventDao: EventDao) {

  val TOKEN_ID_SIZE = 64

  private def generatedId = Random.alphanumeric.take(TOKEN_ID_SIZE).mkString

  /**
   * When creating a token, we check if the same originating ip has already seen a partucular video.
   */
  def newToken(ip: String) = {
    val token = new Token(generatedId, BSONObjectID.generate.stringify)
    val newToken = eventDao.findLastOne(ip, "anonymous-view").map {
      case Some(event) =>
        token.copy(storyIdToShow = Some(event.storyId))
      case _ =>
        token
    }
    newToken.flatMap(tokenDao.create).map(_ => token)
  }
}
