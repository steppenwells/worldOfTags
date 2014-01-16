package models

import akka.actor.{Props, ActorRef, Actor}

class Lobby extends Actor {

  var waitingPlayer: Option[ConnectedPlayer] = None

  def receive = {

    case cp: ConnectedPlayer => {
      waitingPlayer match {
        case None => println(s"player ${cp.player.name} waiting"); waitingPlayer = Some(cp)
        case Some(wp) => {
          println(s"connecting ${wp.player.name} to ${cp.player.name}, game START")
          val game = context.actorOf(Props(classOf[GameActor], wp, cp))
          waitingPlayer = None
        }
      }
    }
  }
}

case class ConnectedPlayer(player: Player, connection: ActorRef)

