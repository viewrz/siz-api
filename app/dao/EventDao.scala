package dao

import javax.inject.{Inject, Singleton}
import models.{NewEvent, Event}
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import formats.MongoJsonFormats._

@Singleton
class EventDao @Inject()(val reactiveMongoApi: ReactiveMongoApi) {

  lazy val db = reactiveMongoApi.db

  def collection: JSONCollection = db.collection[JSONCollection]("events")

  // migrate data before app startup and after injection
  updateDB

  def updateDB = {
    collection.indexesManager.ensure(Index(Seq("storyId" -> IndexType.Ascending, "viewerProfileId" -> IndexType.Ascending), name = Some("storyIdViewerProfileIdUniqueIndex"), unique = true, sparse = true))
  }

  def newEventToEvent(newEvent: NewEvent, viewerProfileId: String, tags: List[String], ip: String): Event =
    Event(newEvent.storyId, newEvent._type, tags, viewerProfileId, BSONObjectID.generate.stringify, ip = ip)

  def addEvent(event: Event) = collection.insert(event)

  def findLastOne(ip: String, _type: String) = collection.find(
    Json.obj(
      "ip" -> ip,
      "type" -> _type)
  ).sort(Json.obj("date" -> -1)).one[Event]
}
