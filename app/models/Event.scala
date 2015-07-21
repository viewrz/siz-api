package models

import java.util.Date

// type is a reserved keyword so we must prefix it with an underscore. The mapping then translates it as 'type' .
case class NewEvent(storyId: String, _type: String, date: Date)

case class Event(storyId: String, _type: String, tags: List[String], viewerProfileId: String, id: String, date: Date = new Date()) {
  lazy val tagsWeight = _type match {
    case "like" => 1
    case "nope" => -1
  }
}