package tokenip

import java.util.Date

import play.api.mvc.AnyContentAsEmpty
import play.api.test._
import play.api.test.Helpers._

import actions.{TokenCheckAction, TokenRequest}
import controllers.Stories
import dao.{StoryDao, TokenDao, ViewerProfileDao}
import models._
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._

import org.mockito.Mockito._
import service.StoryService

import scala.concurrent.Future


@RunWith(classOf[JUnitRunner])
class TokenIpSpec extends Specification with Mockito {

  trait Context extends Before {
    val tokenDao = mock[TokenDao]
    val storyDao = mock[StoryDao]
    val storyService = new StoryService(tokenDao, storyDao)

    def before() = {}
  }

  "When a new user (no user account) sees a story in his browser, the mobile app  " should {
    "show the same video" >> {
      "by recording user info each time the token is used" in new Context {

        private val token = new Token("any-id", "any-viewerprofile", None, None, None)

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

        there was one(tokenDao).updateToken(new Token("any-id",
          "any-viewerprofile",
          None,
          Some("the-remote-ip"),
          Some("token-ip-story-id")))

      }
      "when creating a token, check the last seen videostrip for that ip and bind it to the token properties" in new Context {

        private val token = new Token("any-id", "any-viewerprofile", None, None, None)

        storyService.getBySlug(slug = "the-slug", token, "the-remote-ip")

        there was one(tokenDao).updateToken(new Token("any-id",
          "any-viewerprofile",
          None,
          Some("the-remote-ip"),
          Some("token-ip-story-id")))

      }
    }
  }
}
