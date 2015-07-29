package http

import filters.{TrustAllXForwardedForFilter, CORSFilter}
import play.api.http.HttpFilters

class DefaultFilter extends HttpFilters {
  override val filters = Seq(CORSFilter,TrustAllXForwardedForFilter)
}