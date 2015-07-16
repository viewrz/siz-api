package dao

import javax.inject.{Inject, Singleton}

import models.{Event, ViewerProfile}
import play.api.libs.json.{JsNumber, JsObject, Json}
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import play.modules.reactivemongo.json.collection

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext
/**
 * Created by fred on 16/07/15.
 */
@Singleton
class ViewerProfileDao @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends ReactiveMongoComponents {
  lazy val db = reactiveMongoApi.db

  def collection: JSONCollection = db.collection[JSONCollection]("viewerprofiles")

  def findById(id: String): Future[ViewerProfile] = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[ViewerProfile].collect[List]().map {
    case vp :: Nil =>
      vp
    case _ =>
      ViewerProfile(id)
  }

  def processEvent(event: Event) = {
    val update = if (event.tags.isEmpty) {
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
