package models

import java.util.Date
import reactivemongo.core.commands.LastError

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json._

import scala.concurrent.Future

case class Event(storyId: String, _type: String, tags: List[String], date: Date = new Date()){
  lazy val tagsWeight = _type match {
    case "like" => 1
    case "nope" => -1
  }
}

case class ViewerProfile(id: String, events: List[Event] = Nil,tagsWeights: Map[String,Int] = Map())
{
  def viewedStoryIDs = this.events.map(_.storyId)
  def tagsFilterBy(filter: ((String,Int)) => (Boolean)) = this.tagsWeights.filter(filter)
}

object ViewerProfile extends MongoModel("viewerprofile")
{
  def findById(id: String): Future[ViewerProfile] = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[ViewerProfile].collect[List]().map{
    case vp :: Nil =>
      vp
    case Nil | _ =>
      ViewerProfile(id)
    /* case _ =>
       Error that should send a exception */
  }

  def addEvent(viewerProfileId: String, event: Event) =
    collection.update(
      Json.obj("_id" -> Json.obj("$oid" -> viewerProfileId)),
      Json.obj("$push" ->
        Json.obj("events" -> event),
        "$inc" -> JsObject(event.tags.map(tag => ("tagsWeights."+tag, JsNumber(event.tagsWeight))))
      ),
      multi = false,
      upsert = true
    )
}