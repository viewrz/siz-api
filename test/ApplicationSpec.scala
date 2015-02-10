import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.JsObject

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "no route for bad api URL" in new WithApplication{
      val badRequest = route(FakeRequest(GET, "/bad"))
      badRequest must beNone
    }

    "render the index page without token" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(UNAUTHORIZED)
      contentType(home) must beSome.which(_ == "application/json")
      contentAsString(home) must contain("""errors""")
    }

    "render the index page with token" in new WithApplication{
      val createdToken = contentAsJson(route(FakeRequest(POST, "/tokens").withJsonBody(JsObject(Seq()))).get)

      val home = route(FakeRequest(GET, "/").withHeaders(("X-Access-Token",(createdToken \ "tokens" \ "id").as[String]))).get

      status(home) must equalTo(UNAUTHORIZED)
      contentType(home) must beSome.which(_ == "application/json")
      contentAsString(home) must contain("""links""")
    }

  }
}
