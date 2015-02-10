package models

import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class ViewerProfile(id: String, viewedStoryIds: List[String] = List(), tagsWeights: Map[String,Int] = Map())
{
  def tagsFilterBy(filter: ((String,Int)) => (Boolean)) = this.tagsWeights.filter(filter)
}

object ViewerProfile extends MongoModel("viewerprofile")
{
  def findById(id: String): Future[ViewerProfile] = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[ViewerProfile].collect[List]().map{
    case vp :: Nil =>
      vp
    case _ =>
      ViewerProfile(id)
  }

  def processEvent(event: Event) =
    collection.update(
      Json.obj("_id" -> Json.obj("$oid" -> event.viewerProfileId)),
      Json.obj(
        "$addToSet" -> Json.obj("viewedStoryIds" -> event.storyId),
        "$inc" -> JsObject(event.tags.map(tag => ("tagsWeights."+tag, JsNumber(event.tagsWeight))))
      ),
      multi = false,
      upsert = true
    )
}