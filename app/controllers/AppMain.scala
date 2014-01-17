package controllers

import play.api.mvc.{WebSocket, Action, Controller}
import play.api.libs.json.JsValue
import play.api.libs.concurrent.Akka
import akka.actor.Props
import models._
import play.api.libs.iteratee.{Iteratee, Concurrent}
import play.api.Play.current
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import models.PlayerMessage
import models.Echo

object AppMain extends Controller {

  val lobby = Akka.system.actorOf(Props[Lobby])

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

  def game() = Action { req =>
    val name = req.body.asFormUrlEncoded.flatMap(_.get("name")).flatMap(_.headOption)
    Ok(views.html.Application.game(name.get))
  }

  def content() = Action.async { req =>
    val tags = req.body.asFormUrlEncoded.flatMap(_.get("tags")).flatMap(_.headOption)
    println(tags.get)
    val content = ContentApi.getContent(tags.get)
    content.map(c => Ok(views.html.Application.content(c, tags.get.split(","))))
  }

  def gameConnection() = WebSocket.using[String] { request =>

    //Concurernt.broadcast returns (Enumerator, Concurrent.Channel)
      val (out,channel) = Concurrent.broadcast[String]

      val playerConnection = Akka.system.actorOf(Props(classOf[PlayerConnection], channel))

        //log the message to stdout and send response back to client
      val in = Iteratee.foreach[String] {msg =>
        println(msg)
        val parts = msg.split(":")

        playerConnection ! PlayerMessage(parts(0), parts(1))
      }

      (in,out)
    }
}