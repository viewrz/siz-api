package http

import javax.inject.Inject

import filters.CORSFilter
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter

/**
 * Created by fred on 16/07/15.
 */
class DefaultFilter extends HttpFilters {

  override val filters = Seq(CORSFilter)
}