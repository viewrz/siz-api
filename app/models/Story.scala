package models

import java.util.Date

import reactivemongo.api.QueryOpts
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Json
import play.modules.reactivemongo.json.ImplicitBSONHandlers._

import scala.concurrent.ExecutionContext.Implicits.global

case class VideoFormat(href: String, _type: String)
case class Box(height: Int, width: Int, formats: List[VideoFormat])
case class Image(href: String)

case class Source(id: String,
                  _type: String,
                  duration: Option[Long])

case class Story(boxes: List[Box],
                 creationDate: Date,
                 id: String,
                 slug: String,
                 source: Source,
                 picture: Image,
                 title: String,
                 tags: List[String])

object Story extends MongoModel("stories"){
  def findRecommends(limit: Int, orderBy: String = "creationDate", exceptStoryIds: List[String] = List(), exceptTags: List[String] = List()) =
      collection.find(Json.obj("_id" ->  Json.obj("$nin" -> exceptStoryIds.map(id => Json.obj("$oid" -> id))),
                               "tags" -> Json.obj("$not" -> Json.obj("$elemMatch" -> Json.obj("$in" -> exceptTags)))
      )).options(QueryOpts().batchSize(limit)).sort( Json.obj(orderBy -> -1) ).cursor[Story].collect[List](limit)
  def getByIds(ids: List[String]) =
      collection.find(Json.obj("_id" ->  Json.obj("$in" -> ids.map(id => Json.obj("$oid" -> id))))).cursor[Story].collect[List]()


  def getBySlug(slug: String) = collection.find(Json.obj("slug" -> slug)).cursor[Story].collect[List]().map(_.headOption)
  def getById(id: String) = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[Story].collect[List]().map(_.headOption)
}
