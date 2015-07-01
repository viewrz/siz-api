package models

import java.util.Date

import models.Event._
import play.api.PlayException
import reactivemongo.api.QueryOpts
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Json
import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import play.api.Play.current
import utils.{Queue, Slug}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class VideoFormat(href: String, _type: String)
case class Box(height: Option[Int],
               width: Option[Int],
               start: Option[Long],
               stop: Option[Long],
               formats: Option[List[VideoFormat]])
case class NewBox(start: Long,
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
                 tags: List[String],
                 privacy: String)

case class NewStory(boxes: List[NewBox],
                 source: Source,
                 title: String,
                 tags: List[String])

object Story extends MongoModel("stories") {
  def updateDB = {
    collection.indexesManager.ensure(Index(Seq("slug" -> IndexType.Ascending,"slug" -> IndexType.Ascending), name = Some("slugUniqueIndex"), unique = true))
    collection.update(Json.obj("privacy" -> Json.obj("$exists" -> false)), Json.obj("$set" -> Json.obj("privacy" -> "public")),multi = true)
  }

  def findRecommends(limit: Int, orderBy: String = "creationDate", exceptStoryIds: List[String] = List(), exceptTags: List[String] = List()) =
      collection.find(Json.obj("_id" ->  Json.obj("$nin" -> exceptStoryIds.map(id => Json.obj("$oid" -> id))),
                               "tags" -> Json.obj("$not" -> Json.obj("$elemMatch" -> Json.obj("$in" -> exceptTags))),
                                "privacy" -> Json.obj("$nin" -> Json.arr(List("unlisted","private")))
      )).options(QueryOpts().batchSize(limit)).sort( Json.obj(orderBy -> -1) ).cursor[Story].collect[List](limit)
  def getByIds(ids: List[String]) =
      collection.find(Json.obj("_id" ->  Json.obj("$in" -> ids.map(id => Json.obj("$oid" -> id))))).cursor[Story].collect[List]()

  private def newBoxToBox(newBox: NewBox) =
    Box(height = None,
    width = None,
    start = Some(newBox.start),
    stop = Some(newBox.stop),
    formats = None)

  private def pictureFromSource(source: Source) = Image(s"//img.youtube.com/vi/${source.id}/0.jpg")

  def newStoryToStory(newStory: NewStory, slug: String) =
    Story(boxes = newStory.boxes.map(newBoxToBox),
      creationDate = new Date(),BSONObjectID.generate.stringify,
      slug = slug,
      source = newStory.source,
      picture = pictureFromSource(newStory.source),
      title = newStory.title,
      tags = newStory.tags,
      privacy = "unlisted")

  def getBySlug(slug: String) = collection.find(Json.obj("slug" -> slug)).cursor[Story].collect[List]().map(_.headOption)
  def getById(id: String) = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[Story].collect[List]().map(_.headOption)

  private def generateValidSlug(title: String) = {
    val slug = Slug.slugify(title)
    val maximumTry = 10

    def checkSlug(i: Int): Future[String] = {
      if (i > maximumTry) {
        throw new PlayException("Story Error", s"$i first slugs generated for $slug already used")
      }
      val testedSlug = if (i==0) slug else s"$slug-$i"
      getBySlug(testedSlug).flatMap {
        case Some(_) =>
          checkSlug(i + 1)
        case None =>
          Future.successful(testedSlug)
      }
    }
    checkSlug(0)
  }

  def generateStory(newStory: NewStory) = {
    generateValidSlug(newStory.title).flatMap { slug =>
      val story = newStoryToStory(newStory, slug)
      Queue.send("generate.story", Json.toJson(story)).map(_ => story)
    }
  }
}
