package models

import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.modules.reactivemongo.json._


case class ViewerProfile(id: String, likeStoryIds: List[String] = List(), nopeStoryIds: List[String] = List(), tagsWeights: Option[Map[String,Int]] = None)
{
  def tagsFilterBy(filter: ((String,Int)) => (Boolean)) = this.tagsWeights.map(_.filter(filter).map(_._1).toList).getOrElse(List())
}
