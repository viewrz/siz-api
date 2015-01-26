package play.modules.rethinkdb

import com.rethinkscala.ast.DB
import com.rethinkscala.net.{Async, Version3}
import play.api._
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class RethinkDBPlugin(app: Application) extends Plugin {
  private var _helper: Option[RethinkDBHelper] = None
  def helper = _helper.getOrElse(throw new RuntimeException("RethinkDBPlugin error: no RethinkDBHelper available?"))

  override def onStart {
    Logger info "RethinkDB starting..."
    try {
      val conf = RethinkDBPlugin.parseConf(app)
      _helper = Some(RethinkDBHelper(conf, app))
      Logger.info("RethinkDBPlugin successfully started with db '%s'! Server:\n\t\t%s"
        .format(
          conf.dbname,
          conf.host))
    } catch {
      case NonFatal(e) =>
        throw new PlayException("RethinkDBPlugin Initialization Error", "An exception occurred while initializing the RethinkDBPlugin.", e)
    }
  }

}

object RethinkDBPlugin {
  val DefaultPort = 28015
  val DefaultHost = "localhost"

  case class RethinkDBConf(host: String,port: Int, dbname: String)

  def version(implicit app: Application) = current.helper.version
  def connection(implicit app: Application) = current.helper.connection
  def db(implicit app: Application) = current.helper.db

  def current(implicit app: Application): RethinkDBPlugin = app.plugin[RethinkDBPlugin] match {
    case Some(plugin) => plugin
    case _            => throw new PlayException("RethinkDBPlugin Error", "The RethinkDBPlugin has not been initialized! Please edit your conf/play.plugins file and add the following line: '500:play.modules.rethinkdb.RethinkDBPlugin' (500 is an arbitrary priority and may be changed to match your needs).")
  }

  private def parseConf(app: Application): RethinkDBConf = {
    val host = app.configuration.getString("rethinkdb.host").getOrElse(DefaultHost)
    val port = app.configuration.getInt("rethinkdb.port").getOrElse(DefaultPort)
    app.configuration.getString("rethinkdb.dbname") match {
      case Some(dbname) =>
        RethinkDBConf(host,port,dbname)
      case _ =>
        throw app.configuration.globalError(s"Missing database name in rethinkdb.dbname")
    }
  }
}

private[rethinkdb] case class RethinkDBHelper(conf: RethinkDBPlugin.RethinkDBConf, app: Application) {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  lazy val version = new Version3(conf.host,conf.port)
  lazy val connection = Async(version)
  lazy val db = DB(conf.dbname)
}