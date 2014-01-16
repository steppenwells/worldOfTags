package models

import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent.Channel

class EchoChamber(channel: Channel[String]) extends Actor {

  def receive = {

    case Echo(text) => {
      val tags = ContentApi.getFollowupTags(List(text))
      tags.onSuccess{
        case ts => channel.push(ts.tags.map(_.toJson).mkString("""{ "tags" : [""", ",", "]}"))
      }
    }
  }

}

case class Echo(text: String)
