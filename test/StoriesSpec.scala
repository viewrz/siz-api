import java.util.Date

import dao.StoryDao
import models._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest, WithApplication}
import reactivemongo.bson.BSONObjectID
import spray.http.CacheDirectives.`must-revalidate`

import scala.concurrent.Await
import scala.concurrent.duration._

import play.api.Application

@RunWith(classOf[JUnitRunner])
class StoriesSpec extends Specification {

  val application = FakeApplication()
  private val storyDao = Application.instanceCache[StoryDao].apply(application)

  "Stories" should {
    val unlistedStoryId = BSONObjectID.generate.stringify
    val unlistedStory = Story(boxes = List(),
      creationDate = new Date(), id = unlistedStoryId,
      slug = s"pepper-spray-story-unlisted-$unlistedStoryId", source = Source("9dLmdVDjg1w","youtube",Some(1592000)), picture = Image("http://img.youtube.com/vi/9dLmdVDjg1w/0.jpg"), title = "Pepper Spray",
      tags = List("short-films"),
      privacy = "Unlisted")
    Await.result(storyDao.insert(unlistedStory), 10.0 seconds)
    val listedStoryId = BSONObjectID.generate.stringify
    val listedStory = Story(boxes = List(),
      creationDate = new Date(), id = listedStoryId,
      slug = s"pepper-spray-story-listed-$listedStoryId", source = Source("9dLmdVDjg1w","youtube",Some(1592000)), picture = Image("http://img.youtube.com/vi/9dLmdVDjg1w/0.jpg"), title = "Pepper Spray",
      tags = List("short-films"),
      privacy = "Public")
    Await.result(storyDao.insert(listedStory), 10.0 seconds)


    "Retrieve a unlisted story" in new WithApplication{
      val createdToken = contentAsJson(route(FakeRequest(POST, "/tokens").withJsonBody(JsObject(Seq()))).get)

      val response = route(FakeRequest(GET, s"/stories/$unlistedStoryId").
        withHeaders(("X-Access-Token",(createdToken \ "tokens" \ "id").as[String]))).get

      status(response) must equalTo(OK)
      contentType(response) must  beSome.which(_ == "application/json")
      val jsonStory = contentAsJson(response) \ "stories"
      (jsonStory \ "id").as[String] mustEqual unlistedStoryId
      (jsonStory \ "privacy").as[String] mustEqual "Unlisted"
    }

    "Retrieve recommends story" in new WithApplication{
      val createdToken = contentAsJson(route(FakeRequest(POST, "/tokens").withJsonBody(JsObject(Seq()))).get)

      val response = route(FakeRequest(GET, s"/stories").
        withHeaders(("X-Access-Token",(createdToken \ "tokens" \ "id").as[String]))).get

      status(response) must equalTo(OK)
      contentType(response) must  beSome.which(_ == "application/json")
      val jsonStories = contentAsJson(response) \ "stories"
      jsonStories \\ "id" must not contain JsString(unlistedStoryId)
      jsonStories \\ "id" must contain(JsString(listedStoryId))
    }
  }
}
