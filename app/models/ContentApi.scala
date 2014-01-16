package models

import play.api.libs.ws._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.JsArray
import ExecutionContext.Implicits.global
import scala.util.Random

object ContentApi {

  def refinementFetchUrl(currentTags: String) = s"http://content.guardianapis.com/search.json?page-size=1&tag=$currentTags&show-tags=keyword&show-refinements=keyword&refinement-size=50"
  def tagFetchUrl = s"http://content.guardianapis.com/search.json?page-size=1&show-tags=keyword&show-refinements=keyword&refinement-size=50"

  def getFollowupTags(currentTags: List[String]) = {
    WS.url(refinementFetchUrl(currentTags.mkString(","))).get().map { response =>

      val refinementsListArray = (response.json \ "response" \\ "refinementGroups").head
      val refinementsList = refinementsListArray.asInstanceOf[JsArray].value.head \ "refinements"

      println(refinementsList)

      refinementsList match {
        case arr: JsArray => {
          arr.value.map {refJson =>
            println(refJson)
            Tag(
              (refJson \ "id").as[String],
              (refJson \ "displayName").as[String]
            )
          }.toList
        }
        case _ => Nil
      }
    }
  }

  def getRandomTag = {
    val tagsFuture = WS.url(tagFetchUrl).get().map { response =>

      val refinementsListArray = (response.json \ "response" \\ "refinementGroups").head
      val refinementsList = refinementsListArray.asInstanceOf[JsArray].value.head \ "refinements"

      println(refinementsList)

      refinementsList match {
        case arr: JsArray => {
          arr.value.map {refJson =>
            println(refJson)
            Tag(
              (refJson \ "id").as[String],
              (refJson \ "displayName").as[String]
            )
          }.toList
        }
        case _ => Nil
      }
    }

    tagsFuture.map{ts => ts(Random.nextInt(ts.length))}
  }
}

case class Tag(id: String, name: String) {
  def toJson = s"""{"id" : "$id", "name": "$name"}"""
}