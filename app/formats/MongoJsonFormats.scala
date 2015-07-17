package formats

import models._


object MongoJsonFormats extends CommonJsonFormats {

  import play.api.libs.json._
  import play.api.libs.json.Reads._

  def mongoReadsObjectId[T](r: Reads[T]): Reads[T] = {
    JsPath.json.update((JsPath \ 'id).json.copyFrom((JsPath \ '_id \ '$oid).json.pick[JsString])) andThen r
  }

  def mongoWritesObjectId[T](w: Writes[T]) = OWrites[T] {
    a: T =>
      val js = w.writes(a)
      js.as[JsObject] - "id" ++ Json.obj("_id" -> Json.obj("$oid" -> (js \ "id").get))
  }

  def mongoReadsStringId[T](r: Reads[T]): Reads[T] = {
    JsPath.json.update((JsPath \ 'id).json.copyFrom((JsPath \ '_id).json.pick[JsString])) andThen r
  }

  def mongoWritesStringId[T](w: Writes[T]) = OWrites[T] {
    a: T =>
      val js = w.writes(a)
      js.as[JsObject] - "id" ++ Json.obj("_id" -> (js \ "id").get)
  }

  def withDefault[A](key: String, default: List[String])(r: Reads[A]) = {
    JsPath.json.update((JsPath \ key).json.copyFrom((JsPath \ key).json.pick orElse Reads.pure(Json.toJson(default)))) andThen r
  }

  implicit val tokenRead = mongoReadsStringId[Token](Json.reads[Token])
  implicit val tokenWrite = mongoWritesStringId[Token](Json.writes[Token])
  implicit val userRead = mongoReadsObjectId[User](Json.reads[User])
  implicit val userWrite = mongoWritesObjectId[User](Json.writes[User])

  implicit val eventRead = typeReads[Event](mongoReadsObjectId[Event](Json.reads[Event]))
  implicit val eventWrite = typeWrites[Event](mongoWritesObjectId[Event](Json.writes[Event]))

  implicit val viewerProfileRead = withDefault("nopeStoryIds", List())(withDefault("likeStoryIds", List())(mongoReadsObjectId[ViewerProfile](Json.reads[ViewerProfile])))
  implicit val viewerProfileWrite = mongoWritesObjectId[ViewerProfile](Json.writes[ViewerProfile])
  implicit val videoFormatRead = typeReads[VideoFormat](Json.reads[VideoFormat])
  implicit val videoFormatWrite = typeWrites[VideoFormat](Json.writes[VideoFormat])
  implicit val boxRead = Json.reads[Box]
  implicit val boxWrite = Json.writes[Box]
  implicit val sourceRead = typeReads[Source](Json.reads[Source])
  implicit val sourceWrite = typeWrites[Source](Json.writes[Source])
  implicit val loopRead = Json.reads[Loop]
  implicit val loopWrite = Json.writes[Loop]
  implicit val imageRead = Json.reads[Image]
  implicit val imageWrite = Json.writes[Image]
  implicit val storyRead = mongoReadsObjectId[Story](Json.reads[Story])
  implicit val storyWrite = mongoWritesObjectId[Story](Json.writes[Story])
}
