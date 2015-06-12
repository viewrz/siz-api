import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.JsObject
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}

object TokenHelper {
  def createToken() = route(FakeRequest(POST, "/tokens").withJsonBody(JsObject(Seq()))).get
  def newToken() = (contentAsJson(createToken()) \ "tokens" \ "id").as[String]
}

@RunWith(classOf[JUnitRunner])
class TokensSpec extends Specification {
  "Tokens" should {
    "create a token" in new WithApplication {
      val createdToken = TokenHelper.createToken()
      status(createdToken) must equalTo(CREATED)

      contentType(createdToken) must beSome.which(_ == "application/json")
      val json = contentAsJson(createdToken)

      (json \ "tokens" \ "id").as[String] must not beEmpty
    }
  }

}
