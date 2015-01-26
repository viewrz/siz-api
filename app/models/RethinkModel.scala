package models

import com.rethinkscala._
import play.api.Play.current
import play.modules.rethinkdb.RethinkDBPlugin

class RethinkModel[T <: Document](tableName: String) {
  implicit val connection = RethinkDBPlugin.connection
  implicit val table =  RethinkDBPlugin.db.table[T](tableName)
}
