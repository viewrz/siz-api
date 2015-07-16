package dao

import javax.inject.{Singleton, Inject}

import models.{Event, NewEvent}
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import play.api.libs.concurrent.Execution.Implicits.defaultContext


import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import formats.MongoJsonFormats._

/**
 * Created by fred on 16/07/15.
 */
@Singleton
class EventDao @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends ReactiveMongoComponents {


  lazy val db = reactiveMongoApi.db

  def collection: JSONCollection = db.collection[JSONCollection]("events")

  // migrate data before app startup and after injection
  updateDB

  def updateDB = {
    collection.indexesManager.ensure(Index(Seq("storyId" -> IndexType.Ascending, "viewerProfileId" -> IndexType.Ascending), name = Some("storyIdViewerProfileIdUniqueIndex"), unique = true, sparse = true))
  }

  def newEventToEvent(newEvent: NewEvent, viewerProfileId: String, tags: List[String]): Event = Event(newEvent.storyId, newEvent._type, tags, viewerProfileId, BSONObjectID.generate.stringify)

  def addEvent(event: Event) = collection.insert(event)
}
