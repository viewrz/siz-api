package models

import com.rethinkscala._
import play.api.Play.current
import play.modules.rethinkdb.RethinkDBPlugin

class RethinkModel[T <: Document](tableName: String) {
  implicit val connection = RethinkDBPlugin.connection
  val db = RethinkDBPlugin.db
  implicit val table =  db.table[T](tableName)
}
