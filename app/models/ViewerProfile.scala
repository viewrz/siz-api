package models

case class ViewerProfile(id: String,
                         likeStoryIds: List[String] = List(),
                         nopeStoryIds: List[String] = List(),
                         tagsWeights: Option[Map[String,Int]] = None)
{
  def tagsFilterBy(filter: ((String,Int)) => (Boolean)) = this.tagsWeights.map(_.filter(filter).keys.toList).getOrElse(List())
}
