import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsArray, JsString, JsObject, JsValue}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}

@RunWith(classOf[JUnitRunner])
class EventsSpec extends Specification {

  "Events" should {
    "create a event" in new WithApplication{
      val jsonBody: JsValue = JsObject(
        Seq("events" ->
          JsObject(Seq(
            "storyId" -> JsString("128112222233334dddddaaaa"),
            "type" -> JsString("like"),
            "tags" -> JsArray(Seq(JsString("dogs"),JsString("animals"))))
          )
        )
      )
      val createdToken = contentAsJson(route(FakeRequest(POST, "/tokens").withJsonBody(JsObject(Seq()))).get)

      val createdEvent = route(FakeRequest(POST, "/events").withHeaders(("X-Access-Token",(createdToken \ "tokens" \ "id").as[String])).withJsonBody(jsonBody)).get

      status(createdEvent) must equalTo(NO_CONTENT)
      contentType(createdEvent) must beSome.which(_ == "application/json")
    }
  }
}
