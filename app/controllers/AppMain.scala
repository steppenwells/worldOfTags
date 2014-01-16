package controllers

import play.api.mvc.{Action, Controller}

object AppMain extends Controller {

  def welcome() = Action {
    Ok(views.html.Application.welcome("simple message..."))
  }

  def echo() = Action { Ok(views.html.Application.echo()) }
}