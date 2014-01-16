package controllers

import play.api.mvc.{WebSocket, Action, Controller}
import play.api.libs.json.JsValue
import play.api.libs.concurrent.Akka
import akka.actor.Props
import models.{Echo, EchoChamber}
import play.api.libs.iteratee.{Iteratee, Concurrent}
import play.api.Play.current
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

object AppMain extends Controller {

  def welcome() = Action {
    Ok(views.html.Application.welcome("simple message..."))
  }

  def echo() = Action { Ok(views.html.Application.echo()) }

  /**
   * Handles the chat websocket.
   */
  def echoSocket() = WebSocket.using[String] { request =>

  //Concurernt.broadcast returns (Enumerator, Concurrent.Channel)
    val (out,channel) = Concurrent.broadcast[String]

    val echoer = Akka.system.actorOf(Props(classOf[EchoChamber], channel))

      //log the message to stdout and send response back to client
    val in = Iteratee.foreach[String] {msg =>
      println(msg)
      echoer ! Echo(msg)
    }

    (in,out)
  }
}