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
import controllers.AppMain._

class PlayerConnection(channel: Channel[String]) extends Actor {

  var currentGame: Option[ActorRef] = None

  def receive = {

    case PlayerMessage("connect", name) => {
      currentGame = None
      lobby ! ConnectedPlayer(Player(name), self)
    }

    case PlayerMessage("selectTag", tagId) => {
      currentGame.map{ game => game ! SelectTag(tagId)}
    }

    case JoinedGame(state, gameRef) => {
      currentGame = Some(gameRef)
      channel push state.toJson
    }

    case SyncState(state) => {
      channel push state.toJson
    }
  }

}

case class PlayerMessage(action: String, text: String)
case class JoinedGame(state: GameState, gameActor: ActorRef)
case class SyncState(state: GameState)