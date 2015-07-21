package http

import javax.inject.Inject

import filters.CORSFilter
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter

class DefaultFilter extends HttpFilters {

  override val filters = Seq(CORSFilter)
}