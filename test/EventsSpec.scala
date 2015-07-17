import javax.inject.Inject

import dao.StoryDao
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsArray, JsString, JsObject, JsValue}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import models._
import java.util.Date

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class EventsSpec @Inject() (storyDao: StoryDao) extends Specification {

  "Events" should {
    "create a event" in new WithApplication{
      val storyId = "14339467864855da8fe28614"
      val newStory = Story(boxes = List(), creationDate = new Date(), id = storyId,
        slug = "pepper-spray-events", source = Source("9dLmdVDjg1w","youtube",Some(1592000)), picture = Image("http://img.youtube.com/vi/9dLmdVDjg1w/0.jpg"), title = "Pepper Spray",
        tags = List("short-films"),
        privacy = "Unlisted")
      Await.result(storyDao.insert(newStory), 1.0 seconds)

      val jsonBody: JsValue = JsObject(
        Seq("events" ->
          JsObject(Seq(
            "storyId" -> JsString(storyId),
            "type" -> JsString("like")
            )
          )
        )
      )

      val createdToken = contentAsJson(route(FakeRequest(POST, "/tokens").withJsonBody(JsObject(Seq()))).get)

      val createdEvent = route(FakeRequest(POST, "/events").withHeaders(("X-Access-Token",(createdToken \ "tokens" \ "id").as[String])).withJsonBody(jsonBody)).get

      status(createdEvent) must equalTo(CREATED)
      contentType(createdEvent) must  beSome.which(_ == "application/json")
      val jsonEvent = contentAsJson(createdEvent) \ "events"
      jsonEvent \ "tags" mustEqual JsArray(List(JsString("short-films")))
      jsonEvent \ "storyId" mustEqual JsString(storyId)
      jsonEvent \ "type" mustEqual JsString("like")
    }
  }
}
