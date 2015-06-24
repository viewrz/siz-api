package models

import java.util.Date

import play.modules.aws.SQSPlugin
import reactivemongo.api.QueryOpts
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Json
import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

case class VideoFormat(href: String, _type: String)
case class Box(height: Int,
               width: Int,
               start: Option[Long],
               stop: Option[Long],
               formats: List[VideoFormat])
case class NewBox(height: Int,
               width: Int,
               start: Long,
               stop: Long)
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

case class NewStory(boxes: List[NewBox],
                 slug: String,
                 source: Source,
                 picture: Image,
                 title: String,
                 tags: List[String])

object Story extends MongoModel("stories") {
  def findRecommends(limit: Int, orderBy: String = "creationDate", exceptStoryIds: List[String] = List(), exceptTags: List[String] = List()) =
      collection.find(Json.obj("_id" ->  Json.obj("$nin" -> exceptStoryIds.map(id => Json.obj("$oid" -> id))),
                               "tags" -> Json.obj("$not" -> Json.obj("$elemMatch" -> Json.obj("$in" -> exceptTags)))
      )).options(QueryOpts().batchSize(limit)).sort( Json.obj(orderBy -> -1) ).cursor[Story].collect[List](limit)
  def getByIds(ids: List[String]) =
      collection.find(Json.obj("_id" ->  Json.obj("$in" -> ids.map(id => Json.obj("$oid" -> id))))).cursor[Story].collect[List]()

  def newBoxToBox(newBox: NewBox) =
    Box(height = newBox.height,
    width = newBox.width,
    start = Some(newBox.start),
    stop = Some(newBox.stop),
    formats = List())

  def newStoryToStory(newStory: NewStory) =
    Story(boxes = newStory.boxes.map(newBoxToBox),
      creationDate = new Date(),BSONObjectID.generate.stringify,
      slug = newStory.slug,
      source = newStory.source,
      picture = newStory.picture,
      title = newStory.title,
      tags = newStory.tags)

  def getBySlug(slug: String) = collection.find(Json.obj("slug" -> slug)).cursor[Story].collect[List]().map(_.headOption)
  def getById(id: String) = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[Story].collect[List]().map(_.headOption)

  private def getQueue() = SQSPlugin.json("aws.sqs.queues.story")
  def generateStory(newStory: NewStory) = {
    val story = newStoryToStory(newStory)
    getQueue().send(Json.toJson(story)).map(_ => story)
  }
}
