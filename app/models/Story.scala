package models

import java.util.Date

import com.rethinkscala.Document
import com.rethinkscala.Implicits.Async._
import utils.Hash
import scala.concurrent.ExecutionContext.Implicits.global


case class OldBoxe(height: Int, width: Int, url: String)
case class OldStory(boxes: Option[List[OldBoxe]],
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
case class Boxe(height: Int, width: Int, formats: List[VideoFormat])
case class Image(href: String)

object Boxe {
  def oldBoxesToBoxes(oldBoxes: List[OldBoxe], s: OldStory): List[Boxe] = s.gifUrls match {
    case None =>
      oldBoxes.map(oldBoxeToBoxe)
    case Some(gifUrls) =>
      (oldBoxes zip gifUrls) map { t => Boxe(t._1.height,t._1.width,List(VideoFormat(t._1.url,"mp4"),VideoFormat(t._2,"gif")) ) }
  }
  def oldBoxeToBoxe(b: OldBoxe) = Boxe(b.height,b.width,List(VideoFormat(b.url,"mp4")))
}

case class Source(id: String,
                  _type: String,
                  duration: Long)
/*,
                  start: Option[Long],
                  stop: Option[Long])*/
case class Story(boxes: List[Boxe],
                 creationDate: Date,
                 id: String,
                 slug: String,
                 source: Source,
                 picture: Image,
                 title: String)

object OldStory extends RethinkModel[OldStory]("video"){
  def newIdToOldId(newId: String) = newId.substring(0,13).toLong
  def getAll(limit: Int, orderBy: String) = table.filter(f=> f.hasFields("boxes")).orderBy(orderBy.desc).limit(limit).as[Seq[OldStory]]
  def getBySlug(slug: String) = table.get(slug).as[OldStory]
  def getById(id: String) = table.filter(Map("date" -> newIdToOldId(id))).as[Seq[OldStory]].map(_.headOption)
}

object Story {
  def oldIdToNewId(oldId: String, date: Date) = date.getTime.toString+Hash.md5(oldId).substring(0,11)
  def oldStoriesToStories(oldStories: Seq[OldStory]): Seq[Story] = oldStories.map(Story.oldStoryToStory)
  def oldStoryToStory(s: OldStory) = Story(Boxe.oldBoxesToBoxes(s.boxes.get, s), s.date, oldIdToNewId(s.id,s.date),s.id, Source.oldStoryToSource(s), Image(s.pictureUrl), s.title)
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
      doubleSecondsToLongMs(s.duration)
       /*,
      s.start.map(doubleSecondsToLongMs), //.map(floatSecondsToIntMs),
      s.stop.map(doubleSecondsToLongMs) //.map(floatSecondsToIntMs) */
    )
}