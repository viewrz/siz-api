package services

import java.util.Date

import dao.{EventDao, StoryDao, TokenDao}
import models._
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

import scala.concurrent.duration._
import scala.concurrent.Await

@RunWith(classOf[JUnitRunner])
class ShowLastStoryToNewUserSpec extends Specification with Mockito {

  trait Context extends Before {
    val storyDao = mock[StoryDao]
    val eventDao = mock[EventDao]
    val tokenDao = mock[TokenDao]
    val storyService = new StoryService(eventDao, storyDao, tokenDao)

    val tokenService = new TokenService(tokenDao, eventDao)

    def before() = {}
  }

  "When a new user (no user account) sees a story in his browser, the mobile app  " should {
    val storyToShow = Story(
      boxes = List(),
      creationDate = new Date(),
      id = "story-id-to-show",
      slug = "the-slug",
      source = Source("9dLmdVDjg1w", "youtube", Some(1592000)),
      picture = Image("http://img.youtube.com/vi/9dLmdVDjg1w/0.jpg"),
      title = "Pepper Spray",
      tags = List("short-films"),
      privacy = "Unlisted")

    "show the same video" >> {
      "by recording user info each time he gets a video by slug" in new Context {

        private val token = Token("any-id", "any-viewerprofile", None)

        storyDao.getBySlug("the-slug") returns Future.successful(Option(storyToShow))
        storyService.getBySlug(slug = "the-slug", token, "the-remote-ip")

        there was one(eventDao).addEvent(any[Event])
      }
      """when creating a token, check the last seen videostrip for
        |that ip and bind it to the token properties""".stripMargin in new Context {

        val event = Event("story-id-to-show",
          "anonymous-view",
          List(),
          "any-viewerProfileId",
          ip = "ip-that-visit-story-id"
        )

        eventDao.findLastOne("ip-that-visit-story-id", "anonymous-view") returns Future.successful(Option(event))
        tokenDao.create(any[Token]) returns Future.successful{ mock[WriteResult] }

        val token = Await.result(tokenService.newToken("ip-that-visit-story-id"), 1.0 seconds)
        token.storyIdToShow must beSome.which(_ == "story-id-to-show")
      }

      """the first time recommends is called the stories to show is
        |first on the return list and it's remove from the token""".stripMargin in new Context {
        val token = Token("any-id",
          "any-viewerprofile",
          None,
          storyIdToShow = Some("story-id-to-show")
        )

        val viewerProfil = ViewerProfile("viewerProfileIds")

        storyDao.getById("story-id-to-show") returns Future.successful(Option(storyToShow))
        storyDao.findRecommends(anyInt, anyString, any[List[String]], any[List[String]]) returns Future.successful(List())

        val stories = Await.result(storyService.findRecommends(10,"creationDate",viewerProfil,token), 1.0 seconds)
        stories.head mustEqual storyToShow

        there was one(tokenDao).update(token.copy(storyIdToShow = None))
      }
    }
  }
}
