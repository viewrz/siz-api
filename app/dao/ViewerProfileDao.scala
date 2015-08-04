package dao

import javax.inject.{Inject, Singleton}

import models.{Event, ViewerProfile}
import play.api.libs.json.{JsNumber, JsObject, Json}
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import formats.MongoJsonFormats._

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.util.Success

@Singleton
class ViewerProfileDao @Inject()(val reactiveMongoApi: ReactiveMongoApi) {
  lazy val db = reactiveMongoApi.db

  def collection: JSONCollection = db.collection[JSONCollection]("viewerprofiles")

  def findById(id: String): Future[ViewerProfile] = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[ViewerProfile]().collect[List]().map {
    case vp :: Nil =>
      vp
    case _ =>
      ViewerProfile(id)
  }

  def updateFromEvent(event: Event) = {
    val updateQuery = if (event.tags.isEmpty) {
      Json.obj(
        "$pull" -> Json.obj((if(event._type == "Nope") "Like" else "Nope") + "StoryIds" -> event.storyId),
        "$addToSet" -> Json.obj(event._type + "StoryIds" -> event.storyId)
      )
    } else {
      Json.obj(
        "$pull" -> Json.obj((if(event._type == "Nope") "Like" else "Nope") + "StoryIds" -> event.storyId),
        "$addToSet" -> Json.obj(event._type + "StoryIds" -> event.storyId),
        "$inc" -> JsObject(event.tags.map(tag => ("tagsWeights." + tag, JsNumber(event.tagsWeight))))
      )
    }
    collection.update(
      Json.obj(
        "_id" -> Json.obj(
          "$oid" -> event.viewerProfileId
        ),
        "LikeStoryIds" -> Json.obj(
          "$not" -> Json.obj(
            "$elemMatch" -> Json.obj("$eq" -> event.storyId)
          )
        ),
        "NopeStoryIds" -> Json.obj(
          "$not" -> Json.obj(
            "$elemMatch" -> Json.obj("$eq" -> event.storyId)
          )
        )
      ),
      updateQuery,
      multi = false
    ).map { r => Success(r.n == 1) }
  }

  def insert(viewerProfile: ViewerProfile) =
    collection.insert(viewerProfile).map(r => Success(r.n == 1))

}
