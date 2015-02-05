import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class UsersSpec extends Specification {

  "Users" should {

    "send 404 on /users" in new WithApplication{
      route(FakeRequest(GET, "/users")) must beNone
    }

    "create a email user v1.0" in new WithApplication{
      val jsonBody: JsValue = JsObject(
        Seq("users" ->
          JsObject(Seq(
            "email" -> JsString("johndoe-v1.0@siz.io"),
            "password" -> JsString("6b3a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e"),
            "username" -> JsString("johndoev1.0"))
          )
        )
      )
      val createUser = route(FakeRequest(POST, "/users").withJsonBody(jsonBody)).get

      status(createUser) must equalTo(CREATED)
      contentType(createUser) must beSome.which(_ == "application/json")
      contentAsString(createUser) must contain ("""email""")
      contentAsString(createUser) must not contain ("""password""")
    }

    "create a email user v1.1" in new WithApplication{
      val jsonBody: JsValue = JsObject(
        Seq("users" ->
          JsObject(Seq(
            "email" -> JsString("johndoe-v1.1@siz.io"),
            "password" -> JsString("6b3a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e"),
            "username" -> JsString("johndoev1.1"))
          )
        )
      )
      val createdToken = contentAsJson(route(FakeRequest(POST, "/tokens").withJsonBody(JsObject(Seq()))).get)

      val createdUser = route(FakeRequest(POST, "/users").withHeaders(("X-Access-Token",(createdToken \ "tokens" \ "id").as[String])).withJsonBody(jsonBody)).get

      status(createdUser) must equalTo(CREATED)
      contentType(createdUser) must beSome.which(_ == "application/json")
      contentAsString(createdUser) must contain ("""email""")
      contentAsString(createdUser) must not contain ("""password""")
    }
  }
}
