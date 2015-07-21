package dto

import java.util.Date

import models._
import reactivemongo.bson.BSONObjectID

case class NewBox(start: Long,
                  stop: Long) {
  def toBox =
    Box(height = None,
      width = None,
      start = Some(start),
      stop = Some(stop),
      formats = None)
}

case class NewStory(boxes: List[NewBox],
                    source: Source,
                    title: String,
                    tags: List[String]) {
  def toStory(slug: String) =
    Story(boxes = boxes.map(_.toBox),
      creationDate = new Date(), BSONObjectID.generate.stringify,
      slug = slug,
      source = source,
      picture = Image(s"//img.youtube.com/vi/${source.id}/0.jpg"),
      title = title,
      tags = tags,
      privacy = "Unlisted",
      loop = None)
}
