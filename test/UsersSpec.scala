import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json._
import play.api.mvc

import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future

object UserHelper {
  def createUser(email: String, password: String, username: String) = {
    val jsonBody: JsValue = JsObject(
      Seq("users" ->
        JsObject(Seq(
          "email" -> JsString(email),
          "password" -> JsString(password),
          "username" -> JsString(username))
        )
      )
    )
    val token = TokenHelper.newToken()
    route(FakeRequest(POST, "/users").withHeaders(("X-Access-Token", token)).withJsonBody(jsonBody)).get
  }

  def login(login: String, password: String, _type: String = "email") = {
    val jsonBody: JsValue = JsObject(
      Seq("users" ->
        JsObject(Seq(
          _type -> JsString(login),
          "password" -> JsString(password))
        )
      )
    )
    val token = TokenHelper.newToken()
    route(FakeRequest(PUT, s"/tokens/$token").withHeaders(("X-Access-Token",token)).withJsonBody(jsonBody)).get
  }
}

@RunWith(classOf[JUnitRunner])
class UsersSpec extends Specification {
  val email = "johndoe@siz.io"
  val password = "6b3a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e"
  val username = "johndoev1.1"

  "Users" should {

    "send 404 on /users" in new WithApplication{
      private val response = route(FakeRequest(GET, "/users")).get
      // /users is not accesible via GET
      private val json = contentAsJson(response)

      status(response) must equalTo(NOT_FOUND)
      contentType(response) must beSome.which(_ == "application/json")

      json \ "errors" mustNotEqual JsUndefined

    }

    "create a email user" in new WithApplication {
      val createdUser = UserHelper.createUser(email,password,username)

      status(createdUser) must equalTo(CREATED)
      contentType(createdUser) must beSome.which(_ == "application/json")
      contentAsString(createdUser) must contain ("""email""")
      contentAsString(createdUser) must not contain ("""password""")
    }

    "create a email user with invalid email" in new WithApplication {
      val createdUser = UserHelper.createUser("oh_my_bad@this_is_not_a_email",password,username)

      status(createdUser) must equalTo(BAD_REQUEST)
      contentType(createdUser) must beSome.which(_ == "application/json")
      contentAsString(createdUser) must contain ("""email""")
      contentAsString(createdUser) must not contain ("""password""")
      contentAsString(createdUser) must not contain ("""username""")
    }

    "create a email user with invalid password" in new WithApplication {
      val createdUser = UserHelper.createUser(email,"invalidPassword",username)

      status(createdUser) must equalTo(BAD_REQUEST)
      contentType(createdUser) must beSome.which(_ == "application/json")
      contentAsString(createdUser) must not contain ("""email""")
      contentAsString(createdUser) must contain ("""password""")
      contentAsString(createdUser) must not contain ("""username""")
    }

    "create a email user with invalid username" in new WithApplication {
      val createdUser = UserHelper.createUser(email,password,"forbidden___username")

      status(createdUser) must equalTo(BAD_REQUEST)
      contentType(createdUser) must beSome.which(_ == "application/json")
      contentAsString(createdUser) must not contain ("""email""")
      contentAsString(createdUser) must not contain ("""password""")
      contentAsString(createdUser) must contain ("""username""")
    }

    "create a email user with alreaded register email" in new WithApplication {
      val createdUser = UserHelper.createUser(email,password,"othername")

      status(createdUser) must equalTo(CONFLICT)
      contentType(createdUser) must beSome.which(_ == "application/json")
      contentAsString(createdUser) must contain ("""email""")
      contentAsString(createdUser) must not contain ("""password""")
      contentAsString(createdUser) must not contain ("""username""")
    }

    "create a email user with alreaded register username" in new WithApplication {
      val createdUser = UserHelper.createUser("othermail@siz.io",password,username)

      status(createdUser) must equalTo(CONFLICT)
      contentType(createdUser) must beSome.which(_ == "application/json")
      contentAsString(createdUser) must not contain ("""email""")
      contentAsString(createdUser) must not contain ("""password""")
      contentAsString(createdUser) must contain ("""username""")
    }

    "login by email" in new WithApplication {
      val loggedUser = UserHelper.login(email, password, "email")
      status(loggedUser) must equalTo(OK)
      contentType(loggedUser) must beSome.which(_ == "application/json")
    }

    "incorrect email when login by email" in new WithApplication {
      val loggedUser = UserHelper.login(s"incorrect-${email}", password, "email")
      status(loggedUser) must equalTo(NOT_FOUND)
      contentType(loggedUser) must beSome.which(_ == "application/json")
    }

    "incorrect password when login by email" in new WithApplication {
      val loggedUser = UserHelper.login(email, "ae8a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e", "email")
      status(loggedUser) must equalTo(NOT_FOUND)
      contentType(loggedUser) must beSome.which(_ == "application/json")
    }

    "login by username" in new WithApplication {
      val loggedUser = UserHelper.login(username, password, "username")
      status(loggedUser) must equalTo(OK)
      contentType(loggedUser) must beSome.which(_ == "application/json")
    }

    "incorrect username when login by username" in new WithApplication {
      val loggedUser = UserHelper.login(s"incorrect${username}", password, "username")
      status(loggedUser) must equalTo(NOT_FOUND)
      contentType(loggedUser) must beSome.which(_ == "application/json")
    }

    "incorrect password when login by email" in new WithApplication {
      val loggedUser = UserHelper.login(username, "ae8a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e", "username")
      status(loggedUser) must equalTo(NOT_FOUND)
      contentType(loggedUser) must beSome.which(_ == "application/json")
    }
  }
}
