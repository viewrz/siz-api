package formats

import models._
import play.api.data.validation.ValidationError
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.util.Date

trait APIJsonFormats extends CommonJsonFormats {
  def addHref[T](objType: String, w : Writes[T]): Writes[T] = w.transform {
    js =>
      js.as[JsObject] ++
      Json.obj("href" -> JsString("/%s/%s".format(objType,(js \ "id").as[String])))
  }

  implicit val tokenWrites: Writes[Token] = addHref("tokens",Json.writes[Token].transform{
    js => js.as[JsObject] - "userId"
  })

  implicit val userWrite: Writes[User] = addHref("users",Json.writes[User].transform( js => js.as[JsObject] - "passwordHash" - "facebookToken"))

  val Sha256Regex = "[0-9a-z]{64}".r
  val EmailRegex = """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$""".r
  val UsernameRegex = "[0-9a-zA-Z.]{2,20}".r
  val FacebookTokenRegex = "[^;\t\n]{1,1024}".r
  val StoryIdRegex = "[0-9]{13}[0-9a-z]{11}".r
  val TagsRegex = "[0-9a-z-]{1,50}".r

  implicit val newUserRead: Reads[NewUser]  = (
    (__ \ "email").readNullable[String](pattern(EmailRegex, "error.email")) and
    (__ \ "password").readNullable[String](pattern(Sha256Regex, "error.sha256")) and
    (__ \ "username").readNullable[String](pattern(UsernameRegex, "error.username")) and
    (__ \ "facebookToken").readNullable[String](pattern(FacebookTokenRegex, "error.facebookToken"))
    )(NewUser.apply _)

  val likeNopeValidate = Reads.StringReads.filter(ValidationError("error.event.type")){
    List("nope","like").contains(_)
  }

  def isValidTagList(tags: List[String]) = tags.forall(TagsRegex.pattern.matcher(_).matches())

  implicit val eventRead: Reads[NewEvent]  = (
      (__ \ "storyId").read[String](pattern(StoryIdRegex, "error.story.id")) and
      (__ \ "type").read[String](likeNopeValidate) and
      (__ \ "tags").read[List[String]](minLength[List[String]](1) keepAnd filter(ValidationError("error.tag"))(isValidTagList)) and
      (__ \ "date").readNullable[Date].map(_.getOrElse(new Date()))
    )(NewEvent.apply _)

  implicit val loginUserRead: Reads[LoginUser]  = (
    (__ \ "email").readNullable[String](pattern(EmailRegex, "error.email")) and
    (__ \ "password").readNullable[String](pattern(Sha256Regex, "error.sha256")) and
    (__ \ "username").readNullable[String](pattern(UsernameRegex, "error.username")) and
    (__ \ "facebookToken").readNullable[String](pattern(FacebookTokenRegex, "error.facebookToken"))
  )(LoginUser.apply _)

  implicit val errorWrite = Json.writes[Error]
  implicit val emailWrite = addHref("emails",Json.writes[Email])

  implicit val sourceWrite = typeWrites(Json.writes[Source])
  implicit val videoFormatWrite = typeWrites(Json.writes[VideoFormat])
  implicit val imageWrite = Json.writes[Image].transform {
    js =>
      (js \ "href").as[String] match {
        case href: String if href.matches("^//[^/].*") =>
          js.as[JsObject] - "href" ++ Json.obj("href" -> JsString("http:"+href))
        case _ =>
          js.as[JsObject]
      }
  }
  implicit val boxesWrite = Json.writes[Box]
  implicit val storyWrite = addHref("stories",Json.writes[Story])

  implicit val topLevelWrite = Json.writes[TopLevel]
}
