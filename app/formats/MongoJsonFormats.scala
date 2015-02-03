package formats

import models.{Token, User}
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
}
