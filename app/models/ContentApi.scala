package models

import play.api.libs.ws._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.JsArray
import ExecutionContext.Implicits.global
import scala.util.Random

object ContentApi {

  def refinementFetchUrl(currentTags: String) = s"http://content.guardianapis.com/search.json?page-size=1&tag=$currentTags&show-tags=keyword&show-refinements=keyword&refinement-size=50"
  def tagFetchUrl = s"http://content.guardianapis.com/search.json?page-size=1&show-tags=keyword&show-refinements=keyword&refinement-size=50"
  def tagByIdUrl(tagId: String) = s"http://content.guardianapis.com/${tagId}.json?pageSize=1"

  def contentUrl(tagIds: String) = s"http://content.guardianapis.com/search.json?tag=$tagIds&showFields=headline,trailText,thumbnail"

  def getFollowupTags(currentTags: List[String]) = {
    WS.url(refinementFetchUrl(currentTags.mkString(","))).get().map { response =>

      val count = (response.json \ "response" \ "total").as[Int]
      val refinementsListArray = (response.json \ "response" \\ "refinementGroups").headOption
      val refinementsList = refinementsListArray.map(_.asInstanceOf[JsArray].value.head \ "refinements")

      ///println(refinementsList)

      val tags = refinementsList.map{rl => rl match {
          case arr: JsArray => {
            arr.value.map {refJson =>
           //   println(refJson)
              Tag(
                (refJson \ "id").as[String],
                (refJson \ "displayName").as[String]
              )
            }.toList
          }
          case _ => Nil
        }
      }.getOrElse(Nil)
      FollowupData(count, tags)
    }
  }

  def getRandomTag = {
    val tagsFuture = WS.url(tagFetchUrl).get().map { response =>

      val refinementsListArray = (response.json \ "response" \\ "refinementGroups").head
      val refinementsList = refinementsListArray.asInstanceOf[JsArray].value.head \ "refinements"

     // println(refinementsList)

      refinementsList match {
        case arr: JsArray => {
          arr.value.map {refJson =>
           // println(refJson)
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

  def getTag(tagId: String) = {
    WS.url(tagByIdUrl(tagId)).get().map { response =>
      //println(response.json)
      val tagJson = (response.json \ "response" \ "tag")

      val t = Tag(
        (tagJson \ "id").as[String],
        (tagJson \ "webTitle").as[String]
      )
      println(s"read tag $t")
      t
    }
  }

  def getContent(tagIds: String) = {
    WS.url(contentUrl(tagIds)).get().map { response =>
      val contentJson = (response.json \ "response" \ "results")
      //println(contentJson)
      contentJson match {
        case arr: JsArray => {
          arr.value.map { c =>
            Content(
              url = (c \ "webUrl").as[String],
              headline = (c \ "fields" \ "headline").as[String],
              trail = (c \ "fields" \ "trailText").as[String],
              thumb = (c \ "fields" \ "thumbnail").asOpt[String]
            )
          }.toList
        }
        case _ => Nil
      }
    }
  }
}

case class FollowupData(count: Int, tags: List[Tag])

case class Tag(id: String, name: String) {
  def toJson = s"""{"id" : "$id", "name": "$name"}"""
}

case class Content(url: String, headline: String, trail: String, thumb: Option[String])