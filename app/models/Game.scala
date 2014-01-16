package models

import akka.actor.{Props, Actor}
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

class GameActor(one: ConnectedPlayer, two: ConnectedPlayer) extends Actor {

  var state: GameState = null

  for (
    startTag <- ContentApi.getRandomTag;
    availableTags <- ContentApi.getFollowupTags(List(startTag.id))
  ) {

    println(s"starting game start tag is: ${startTag.id}")

    state = GameState(
      playerOne = one.player,
      playerTwo = two.player,
      active = one.player.name,
      pickedTags = List(startTag),
      availableTags = availableTags)

    one.connection ! JoinedGame(state, self)
    two.connection ! JoinedGame(state, self)
  }

  def receive = {
    case s => println(s)
  }
}

case class GameState(
  playerOne: Player,
  playerTwo: Player,
  active: String,
  pickedTags: List[Tag],
  availableTags: List[Tag]
) {
  def toJson = s"""{"playerOne" : ${playerOne.toJson}, "playerTwo" : ${playerTwo.toJson}, "active" : "$active", "pickedTags" : [${pickedTags.map(_.toJson).mkString(",")}], , "availableTags" : [${availableTags.map(_.toJson).mkString(",")}]  }"""
}


case class Player(
  name: String,
  score: Int = 0,
  tricks: List[Trick] = Nil
) {
  def toJson = s"""{"name": "$name", "score": $score, "tricks": [${tricks.map(_.toJson).mkString(",")}]}"""
}

case class Trick(score: Int, tags: List[Tag]) {
  def toJson = s"""{"score: $score, "tags" : [${tags.map(_.toJson).mkString(",")}]}"""
}