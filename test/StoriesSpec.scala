import java.util.Date

import dao.StoryDao
import models._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsString, JsDefined, JsObject}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest, WithApplication}

import scala.concurrent.Await
import scala.concurrent.duration._

import play.api.Application

@RunWith(classOf[JUnitRunner])
class StoriesSpec extends Specification {

  val application = FakeApplication()
  private val storyDao = Application.instanceCache[StoryDao].apply(application)

  "Stories" should {
    "Retrieve a unlisted story" in new WithApplication{
      val storyId = "14339467864855da8fe28615"
      val privacy = "Unlisted"
      val newStory = Story(boxes = List(),
        creationDate = new Date(), id = storyId,
        slug = "pepper-spray-stories", source = Source("9dLmdVDjg1w","youtube",Some(1592000)), picture = Image("http://img.youtube.com/vi/9dLmdVDjg1w/0.jpg"), title = "Pepper Spray",
        tags = List("short-films"),
        privacy = "Unlisted")
      Await.result(storyDao.insert(newStory), 10.0 seconds)

      val createdToken = contentAsJson(route(FakeRequest(POST, "/tokens").withJsonBody(JsObject(Seq()))).get)

      val retrievedStory = route(FakeRequest(GET, s"/stories/$storyId").
        withHeaders(("X-Access-Token",(createdToken \ "tokens" \ "id").as[String]))).get

      status(retrievedStory) must equalTo(OK)
      contentType(retrievedStory) must  beSome.which(_ == "application/json")
      val jsonEvent = contentAsJson(retrievedStory) \ "stories"
      (jsonEvent \ "id").as[String] mustEqual storyId
      (jsonEvent \ "privacy").as[String] mustEqual "Unlisted"
    }
  }
}
