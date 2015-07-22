package service

import javax.inject.Inject

import dao.{EventDao, TokenDao}
import models.Token
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class TokenService @Inject()(tokenDao: TokenDao, eventDao: EventDao) {

  /**
   * When creating a token, we check if the same originating ip has already seen a partucular video.
   * @param token
   * @return
   */
  def create(token: Token, ip: String) = {
    // look for the last story for a given ip
    val newToken = eventDao.findLastOne(ip, "anonymous-view").map {
      case Some(event) =>
        token.copy(storyIdToShow = Some(event.storyId))
      case _ =>
        token
    }
    newToken.flatMap(tokenDao.create)
  }
}
