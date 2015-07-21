package models

import play.api.Logger
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._

import scala.util.Random

case class Token(id: String, viewerProfileId: String, userId: Option[String]= None)