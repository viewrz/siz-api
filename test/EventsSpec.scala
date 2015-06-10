import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsArray, JsString, JsObject, JsValue}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import models._
import java.util.Date

@RunWith(classOf[JUnitRunner])
class EventsSpec extends Specification {

  "Events" should {
    "create a event" in new WithApplication{
      val storyId = "14339467864855da8fe28614"
      val newStory = Story(boxes = List(), creationDate = new Date(), id = storyId,
        slug = "pepper-spray", source = Source("9dLmdVDjg1w","youtube",Some(1592000)), picture = Image("http://img.youtube.com/vi/9dLmdVDjg1w/0.jpg"), title = "Pepper Spray",
        tags = List("short-films"))
      Story.collection.insert(newStory)

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

      status(createdEvent) must equalTo(NO_CONTENT)
      contentType(createdEvent) must beNone
    }
  }
}
