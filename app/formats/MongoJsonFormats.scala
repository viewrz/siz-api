package formats

import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.modules.reactivemongo.json.BSONFormats

trait MongoJsonFormats extends CommonJsonFormats {
  import BSONFormats.BSONObjectIDFormat

  def mongoReadsObjectId[T](r: Reads[T]): Reads[T] = {
    __.json.update((__ \ 'id).json.copyFrom((__ \ '_id \ '$oid).json.pick[JsString] )) andThen r
  }

  def mongoWritesObjectId[T](w : Writes[T]): Writes[T] = {
    w.transform( js => js.as[JsObject] - "id"  ++ Json.obj("_id" -> Json.obj("$oid" -> js \ "id")) )
  }

  def mongoReadsStringId[T](r: Reads[T]): Reads[T] = {
    __.json.update((__ \ 'id).json.copyFrom((__ \ '_id).json.pick[JsString] )) andThen r
  }

  def mongoWritesStringId[T](w : Writes[T]): Writes[T] = {
    w.transform( js => js.as[JsObject] - "id"  ++ Json.obj("_id" -> js \ "id"))
  }

  implicit val tokenRead = mongoReadsStringId[Token](Json.reads[Token])
  implicit val tokenWrite = mongoWritesStringId[Token](Json.writes[Token])
  implicit val userRead = mongoReadsObjectId[User](Json.reads[User])
  implicit val userWrite = mongoWritesObjectId[User](Json.writes[User])
  implicit val eventRead = mongoReadsObjectId[Event](Json.reads[Event])
  implicit val eventWrite = mongoWritesObjectId[Event](Json.writes[Event])
  implicit val viewerProfileRead = mongoReadsObjectId[ViewerProfile](Json.reads[ViewerProfile])
  implicit val viewerProfileWrite = mongoWritesObjectId[ViewerProfile](Json.writes[ViewerProfile])
  implicit val videoFormatRead = typeReads[VideoFormat](Json.reads[VideoFormat])
  implicit val videoFormatWrite = typeWrites[VideoFormat](Json.writes[VideoFormat])
  implicit val boxRead = Json.reads[Box]
  implicit val boxWrite = Json.writes[Box]
  implicit val sourceRead = typeReads[Source](Json.reads[Source])
  implicit val sourceWrite = typeWrites[Source](Json.writes[Source])
  implicit val imageRead = Json.reads[Image]
  implicit val imageWrite = Json.writes[Image]
  implicit val storyRead = mongoReadsObjectId[Story](Json.reads[Story])
  implicit val storyWrite = mongoWritesObjectId[Story](Json.writes[Story])
}
