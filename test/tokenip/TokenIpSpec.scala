package tokenip

import java.util.Date

import dao.{EventDao, StoryDao, TokenDao}
import models._
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import service.{TokenService, StoryService}

import scala.concurrent.Future


@RunWith(classOf[JUnitRunner])
class TokenIpSpec extends Specification with Mockito {

  trait Context extends Before {
    val storyDao = mock[StoryDao]
    val eventDao = mock[EventDao]
    val tokenDao = mock[TokenDao]
    val storyService = new StoryService(eventDao, storyDao)

    val tokenService = new TokenService(tokenDao, eventDao)

    def before() = {}
  }

  "When a new user (no user account) sees a story in his browser, the mobile app  " should {
    "show the same video" >> {
      "by recording user info each time he gets a video by slug" in new Context {

        private val token = new Token("any-id", "any-viewerprofile", None)

        val anyStory = Story(
          boxes = List(),
          creationDate = new Date(),
          id = "token-ip-story-id",
          slug = "the-slug",
          source = Source("9dLmdVDjg1w", "youtube", Some(1592000)),
          picture = Image("http://img.youtube.com/vi/9dLmdVDjg1w/0.jpg"),
          title = "Pepper Spray",
          tags = List("short-films"),
          privacy = "Unlisted")

        storyDao.getBySlug("the-slug") returns Future.successful(Option(anyStory))

        storyService.getBySlug(slug = "the-slug", token, "the-remote-ip")

        there was one(eventDao).addEvent(any[Event])
      }
      """when creating a token, check the last seen videostrip for
        |that ip and bind it to the token properties""".stripMargin in new Context {








      }
    }
  }
}
