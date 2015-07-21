package models

import java.util.Date

case class VideoFormat(href: String, _type: String)

case class Box(height: Option[Int],
               width: Option[Int],
               start: Option[Long],
               stop: Option[Long],
               formats: Option[List[VideoFormat]])

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