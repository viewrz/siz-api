package models

import formats.CommonJsonFormats
import play.api.libs.json.Json
import scala.io
import scala.language.postfixOps


case class Shortlist(id: String, category: String)

object Shortlist extends CommonJsonFormats {
  val shortlistJsonFile = "/shortlist/shortlist.json"

  lazy val shortlists = {
    val shortlistsJson = io.Source.fromInputStream(getClass.getResourceAsStream(shortlistJsonFile)).mkString
    val shortlistsJsPath = Json.parse(shortlistsJson)
    shortlistsJsPath.as[List[Shortlist]].map(s => s.id -> s.category) toMap
  }

  def getAll = shortlists
  def get(id: String) = shortlists.get(id)
}