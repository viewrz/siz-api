package models

import java.util.Date

import play.api.Play.current
import play.api.PlayException
import play.api.libs.json.Json
import reactivemongo.api.QueryOpts
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import utils.{Queue, Slug}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.modules.reactivemongo.json._


case class VideoFormat(href: String, _type: String)

case class Box(height: Option[Int],
               width: Option[Int],
               start: Option[Long],
               stop: Option[Long],
               formats: Option[List[VideoFormat]])

case class NewBox(start: Long,
                  stop: Long)

case class Image(href: String)

case class Loop(formats: List[VideoFormat])

case class Source(id: String,
                  _type: String,
                  duration: Option[Long])

case class Story(boxes: List[Box],
                 creationDate: Date,
                 id: String,
                 slug: String,
                 source: Source,
                 picture: Image,
                 title: String,
                 tags: List[String],
                 privacy: String,
                 loop: Option[Loop] = None)

case class NewStory(boxes: List[NewBox],
                    source: Source,
                    title: String,
                    tags: List[String])
