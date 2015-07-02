package models

import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class ViewerProfile(id: String, likeStoryIds: List[String] = List(), nopeStoryIds: List[String] = List(), tagsWeights: Map[String,Int] = Map())
{
  def tagsFilterBy(filter: ((String,Int)) => (Boolean)) = this.tagsWeights.filter(filter).map(_._1).toList
}

object ViewerProfile extends MongoModel("viewerprofiles")
{
  def findById(id: String): Future[ViewerProfile] = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[ViewerProfile].collect[List]().map{
    case vp :: Nil =>
      vp
    case _ =>
      ViewerProfile(id)
  }

  def processEvent(event: Event) = {
    val update = if(event.tags.nonEmpty) {
      Json.obj(
        "$addToSet" -> Json.obj(event._type + "StoryIds" -> event.storyId)
      )
    } else {
      Json.obj(
        "$addToSet" -> Json.obj(event._type + "StoryIds" -> event.storyId),
        "$inc" -> JsObject(event.tags.map(tag => ("tagsWeights." + tag, JsNumber(event.tagsWeight))))
      )
    }
    collection.update(
      Json.obj("_id" -> Json.obj("$oid" -> event.viewerProfileId)),
      update,
      multi = false,
      upsert = true
    )
  }
}