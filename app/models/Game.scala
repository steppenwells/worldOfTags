package models

import akka.actor.{Props, Actor}
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.util.Random

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
      availableTags = randomTen(availableTags.tags.filterNot(_ == startTag)))

    one.connection ! JoinedGame(state, self)
    two.connection ! JoinedGame(state, self)
  }

  def randomTen(ts: List[Tag]) = {
    Random.shuffle(ts).take(10)
  }

  def receive = {
    case SelectTag(tagId: String) => {
      println("processing move")
      val followUps = for(
        newTag <- ContentApi.getTag(tagId);
        followupTags <- ContentApi.getFollowupTags( state.pickedTags.map(_.id) ::: tagId :: Nil )
      ) yield {
        val tagSet = state.pickedTags ::: newTag :: Nil
        Move(newTag, followupTags.count, followupTags.tags.filterNot(tagSet.contains(_)) )
      }

      followUps onFailure{ case s => s.printStackTrace()}

      followUps onSuccess{ case fut =>
        println("loaded followups")
        fut match {
          case Move(t, c, Nil) => {

            println("no moves available - resetting")

            val trickTags = state.pickedTags ::: t :: Nil
            val trick = Trick(trickTags.length, trickTags)

            val p1 = if(state.playerOne.name == state.active) {
              state.playerOne.copy(
                score = state.playerOne.score + trick.score,
                tricks = trick :: state.playerOne.tricks
              )
            } else {
              state.playerOne
            }

            val p2 = if(state.playerTwo.name == state.active) {
              state.playerTwo.copy(
                score = state.playerTwo.score + trick.score,
                tricks = trick :: state.playerTwo.tricks
              )
            } else {
              state.playerTwo
            }

            val inactivePlayer = if(state.playerOne.name == state.active) state.playerTwo.name else state.playerOne.name

            for (
                startTag <- ContentApi.getRandomTag;
                availableTags <- ContentApi.getFollowupTags(List(startTag.id))
              ) {

              println(s"hand done -- game start tag is: ${startTag.id}")

              state = GameState(
                playerOne = p1,
                playerTwo = p2,
                active = inactivePlayer,
                pickedTags = List(startTag),
                availableTags = randomTen(availableTags.tags.filterNot(_ == startTag)))

              one.connection ! SyncState(state)
              two.connection ! SyncState(state)
            }
          }

          case Move(t, 1, _) => {

            println("one contnet item left - resetting")

            val trickTags = state.pickedTags ::: t :: Nil
            val trick = Trick(trickTags.length, trickTags)

            val p1 = if(state.playerOne.name == state.active) {
              state.playerOne.copy(
                score = state.playerOne.score + trick.score,
                tricks = trick :: state.playerOne.tricks
              )
            } else {
              state.playerOne
            }

            val p2 = if(state.playerTwo.name == state.active) {
              state.playerTwo.copy(
                score = state.playerTwo.score + trick.score,
                tricks = trick :: state.playerTwo.tricks
              )
            } else {
              state.playerTwo
            }

            val inactivePlayer = if(state.playerOne.name == state.active) state.playerTwo.name else state.playerOne.name

            for (
                startTag <- ContentApi.getRandomTag;
                availableTags <- ContentApi.getFollowupTags(List(startTag.id))
              ) {

              println(s"hand done -- game start tag is: ${startTag.id}")

              state = GameState(
                playerOne = p1,
                playerTwo = p2,
                active = inactivePlayer,
                pickedTags = List(startTag),
                availableTags = randomTen(availableTags.tags.filterNot(_ == startTag)))

              one.connection ! SyncState(state)
              two.connection ! SyncState(state)
            }
          }


          case Move(t, c, ats) => {
            println("moves available - following up move")
            val inactivePlayer = if(state.playerOne.name == state.active) state.playerTwo.name else state.playerOne.name

            println(s"hand continues -- available tags: ${ats}")

            state = state.copy(
              active = inactivePlayer,
              pickedTags = state.pickedTags ::: t :: Nil,
              availableTags = randomTen(ats))

            one.connection ! SyncState(state)
            two.connection ! SyncState(state)

          }
        }
      }

    }
    case s => println(s)
  }
}

case class Move(t: Tag, remainingContent: Int, followUps: List[Tag])

case class SelectTag(tagId: String)

case class GameState(
  playerOne: Player,
  playerTwo: Player,
  active: String,
  pickedTags: List[Tag],
  availableTags: List[Tag]
) {
  def toJson = s"""{"playerOne" : ${playerOne.toJson}, "playerTwo" : ${playerTwo.toJson}, "active" : "$active", "pickedTags" : [${pickedTags.map(_.toJson).mkString(",")}], "availableTags" : [${availableTags.map(_.toJson).mkString(",")}]  }"""
}


case class Player(
  name: String,
  score: Int = 0,
  tricks: List[Trick] = Nil
) {
  def toJson = s"""{"name": "$name", "score": $score, "tricks": [${tricks.map(_.toJson).mkString(",")}]}"""
}

case class Trick(score: Int, tags: List[Tag]) {
  def toJson = s"""{"score": $score, "tags" : [${tags.map(_.toJson).mkString(",")}]}"""
}