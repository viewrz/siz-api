package filters

import play.Logger
import play.api.mvc.{Filter, Result, RequestHeader}

import scala.concurrent.Future

object TrustAllXForwardedForFilter extends Filter {
  def apply(nextFilter: (RequestHeader) => Future[Result]
             )(requestHeader: RequestHeader): Future[Result] = {

    val newRequestHeader: RequestHeader =
      requestHeader.headers.get("X-Forwarded-For")
        .flatMap(_.split(',').headOption)
        .map(
          ip => {
            Logger.info(s"Analyzed X-Forwarded-For header and retrieved $ip")
            requestHeader.copy(remoteAddress = ip)
          }
        )
        .getOrElse(requestHeader)

    nextFilter(newRequestHeader)
  }
}
