package models

import java.util.Date

import com.rethinkscala.Document
import com.rethinkscala.Implicits.Async._
import com.rethinkscala.ast.Random
import reactivemongo.api.QueryOpts
import reactivemongo.bson.BSONObjectID
import utils.Hash
import play.api.libs.json.Json
import play.modules.reactivemongo.json.ImplicitBSONHandlers._

import scala.concurrent.ExecutionContext.Implicits.global

case class OldBox(height: Int, width: Int, url: String)
case class OldStory(boxes: Option[List[OldBox]],
                    date: Date,
                    duration: Double,
                    gifUrl: Option[String],
                    gifUrls: Option[List[String]],
                    id: String,
                    pictureUrl: String,
                    playerUrl: String,
                    rank: Double,
                    shortlist: String,
                    source: String,
                    sourceId: String,
                    sourceUrl: String,
                    start: Option[Double],
                    stop: Option[Double],
                    title: String,
                    user: String,
                    voteCount: Int) extends Document

case class VideoFormat(href: String, _type: String)
case class Box(height: Int, width: Int, formats: List[VideoFormat])
case class Image(href: String)

object Box {
  def oldBoxesToBoxes(oldBoxes: List[OldBox], s: OldStory): List[Box] = s.gifUrls match {
    case None =>
      oldBoxes.map(oldBoxToBox)
    case Some(gifUrls) =>
      (oldBoxes zip gifUrls) map { t => Box(t._1.height,t._1.width,List(VideoFormat(t._1.url,"mp4"),VideoFormat(t._2,"gif")) ) }
  }
  def oldBoxToBox(b: OldBox) = Box(b.height,b.width,List(VideoFormat(b.url,"mp4")))
}

case class Source(id: String,
                  _type: String,
                  duration: Option[Long])
/*,
                  start: Option[Long],
                  stop: Option[Long])*/
case class Story(boxes: List[Box],
                 creationDate: Date,
                 id: String,
                 slug: String,
                 source: Source,
                 picture: Image,
                 title: String,
                 tags: List[String])

object OldStory extends RethinkModel[OldStory]("video"){
  val percentWithoutTagsFilter = 20

  def newIdToOldId(newId: String) = newId.substring(0,13).toLong
  def find(limit: Int, orderBy: String, exceptStoryIds: List[String] = List(), exceptTags: List[String] = List()) = {
    val exceptDates = exceptStoryIds.map(OldStory.newIdToOldId)
    table.filter(f => f.hasFields("boxes") and
                      ~(Expr(exceptDates).contains(date => f \ "date" === date))
                      and (~(Expr(exceptTags).contains(tag => f \ "shortlist" === tag)))
                )
         .orderBy(orderBy.desc)
         .limit(limit)
        .as[Seq[OldStory]]
  }
  def getBySlug(slug: String) = table.get(slug).as[OldStory]
  def getById(id: String) = table.filter(Map("date" -> newIdToOldId(id))).as[Seq[OldStory]].map(_.headOption)
}

object Story extends MongoModel("stories"){
  def oldIdToNewId(oldId: String, date: Date) = date.getTime.toString+Hash.md5(oldId).substring(0,11)
  def oldStoriesToStories(oldStories: Seq[OldStory]): Seq[Story] = oldStories.map(Story.oldStoryToStory)
  def oldStoryToStory(s: OldStory) = Story(Box.oldBoxesToBoxes(s.boxes.get, s), s.date, oldIdToNewId(s.id,s.date),s.id, Source.oldStoryToSource(s), Image(s.pictureUrl), s.title, s.shortlist :: Shortlist.get(s.shortlist).map(List(_)).getOrElse(Nil))

  def find(limit: Int, orderBy: String, exceptStoryIds: List[String] = List(), exceptTags: List[String] = List()) =
      collection.find(Json.obj("_id" ->  Json.obj("$nin" -> exceptStoryIds.map(id => Json.obj("$oid" -> id))),
                               "tags" -> Json.obj("$not" -> Json.obj("$elemMatch" -> Json.obj("$in" -> exceptTags)))
      )).options(QueryOpts().batchSize(limit)).sort( Json.obj(orderBy -> -1) ).cursor[Story].collect[List](limit)
  def getBySlug(slug: String) = collection.find(Json.obj("slug" -> slug)).cursor[Story].collect[List]().map(_.headOption)
  def getById(id: String) = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[Story].collect[List]().map(_.headOption)
}

object Source {
  val msInSeconds: Double = 1000
  def doubleSecondsToLongMs(s: Double) = {
    val msFloat: Double = s * msInSeconds
    msFloat.toLong}
  def oldStoryToSource(s: OldStory) =
    Source(
      s.sourceId,
      s.source,
      Some(doubleSecondsToLongMs(s.duration))
       /*,
      s.start.map(doubleSecondsToLongMs), //.map(floatSecondsToIntMs),
      s.stop.map(doubleSecondsToLongMs) //.map(floatSecondsToIntMs) */
    )
}