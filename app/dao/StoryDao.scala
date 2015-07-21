package dao

import java.util.Date
import javax.inject.{Singleton, Inject}

import models.{NewBox, Story, Image, Box}
import models._
import play.api.PlayException
import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.api.QueryOpts
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import utils.{Queue, Slug}

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import formats.MongoJsonFormats._

import play.api.Play.current

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.{Awaitable, Future}

/**
 * Created by fred on 16/07/15.
 */
@Singleton
class StoryDao @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends ReactiveMongoComponents {
  lazy val db = reactiveMongoApi.db

  def collection: JSONCollection = db.collection[JSONCollection]("stories")


  // migrate data before app startup and after injection
  updateDB
  def updateDB = {
    collection.indexesManager.ensure(Index(Seq("slug" -> IndexType.Ascending, "slug" -> IndexType.Ascending), name = Some("slugUniqueIndex"), unique = true))
    collection.update(Json.obj("privacy" -> Json.obj("$exists" -> false)), Json.obj("$set" -> Json.obj("Privacy" -> "Public")), multi = true)
  }

  def findRecommends(limit: Int, orderBy: String = "creationDate", exceptStoryIds: List[String] = List(), exceptTags: List[String] = List()) =
    collection.find(Json.obj("_id" -> Json.obj("$nin" -> exceptStoryIds.map(id => Json.obj("$oid" -> id))),
      "tags" -> Json.obj("$not" -> Json.obj("$elemMatch" -> Json.obj("$in" -> exceptTags))),
      "privacy" -> Json.obj("$nin" -> Json.arr(List("Unlisted", "Private")))
    )).options(QueryOpts().batchSize(limit)).sort(Json.obj(orderBy -> -1)).cursor[Story]().collect[List](limit)

  def getByIds(ids: List[String]) =
    collection.find(Json.obj("_id" -> Json.obj("$in" -> ids.map(id => Json.obj("$oid" -> id))))).cursor[Story]().collect[List]()

  def insert(story: Story) = collection.insert(story)

  private def newBoxToBox(newBox: NewBox) =
    Box(height = None,
      width = None,
      start = Some(newBox.start),
      stop = Some(newBox.stop),
      formats = None)

  private def pictureFromSource(source: Source) = Image(s"//img.youtube.com/vi/${source.id}/0.jpg")

  def newStoryToStory(newStory: NewStory, slug: String) =
    Story(boxes = newStory.boxes.map(newBoxToBox),
      creationDate = new Date(), BSONObjectID.generate.stringify,
      slug = slug,
      source = newStory.source,
      picture = pictureFromSource(newStory.source),
      title = newStory.title,
      tags = newStory.tags,
      privacy = "Unlisted",
      loop = None)

  def getBySlug(slug: String) = collection.find(Json.obj("slug" -> slug)).cursor[Story]().collect[List]().map(_.headOption)

  def getById(id: String) = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[Story]().collect[List]().map(_.headOption)

  private def generateValidSlug(title: String) = {
    val slug = Slug.slugify(title)
    val maximumTry = 10

    def checkSlug(i: Int): Future[String] = {
      if (i > maximumTry) {
        throw new PlayException("Story Error", s"$i first slugs generated for $slug already used")
      }
      val testedSlug = if (i == 0) slug else s"$slug-$i"
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
