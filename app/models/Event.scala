package models

import java.util.Date

import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext.Implicits.global

case class NewEvent(storyId: String, _type: String, tags: List[String], date: Date)

case class Event(storyId: String, _type: String, tags: List[String], viewerProfileId: String, id: String, date: Date = new Date()){
  lazy val tagsWeight = _type match {
    case "like" => 1
    case "nope" => -1
  }
}

object Event extends MongoModel("events")
{
  def ensureIndexes = {
     collection.indexesManager.ensure(Index(Seq("storyId" -> IndexType.Ascending,"viewerProfileId" -> IndexType.Ascending), name = Some("storyIdViewerProfileIdUniqueIndex"), unique = true, sparse = true))
  }

  def newEventToEvent(newEvent: NewEvent, viewerProfileId: String): Event = Event(newEvent.storyId,newEvent._type,newEvent.tags,viewerProfileId, BSONObjectID.generate.stringify)

  def addEvent(event: Event) = collection.insert(event)

}