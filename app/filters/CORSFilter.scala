package filters

import play.api.Play
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object CORSFilter extends Filter  {
  lazy val allow_origin = Play.configuration.getString("cors.allowed.origin")
    .getOrElse("*")
  lazy val allow_headers = Play.configuration.getString("cors.allowed.headers")
    .getOrElse("")
  lazy val allow_methods = Play.configuration.getString("cors.allowed.methods")
    .getOrElse("GET, POST, PUT, DELETE")
  lazy val allow_credentials = Play.configuration.getString("cors.allowed.credentials")
    .getOrElse("false")

  def apply(nextFilter: (RequestHeader) => Future[Result]
             )(requestHeader: RequestHeader): Future[Result] = {
    nextFilter(requestHeader).map{
      _.withHeaders(
        "Access-Control-Allow-Origin" -> allow_origin,
        "Access-Control-Allow-Methods" -> allow_methods,
        "Access-Control-Allow-Headers" -> allow_headers,
        "Access-Control-Allow-Credentials" -> allow_credentials
      )
    }
  }
}
