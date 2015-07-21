package controllers

import javax.inject.Singleton

import play.api.mvc._

@Singleton
class Options extends Controller {
  def all(path: String) = Action {
    Ok
  }
}