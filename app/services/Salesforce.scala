package services

import models.{Tweet, User}
import play.api.Application
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.{WSResponse, WSRequestHolder, WS}
import play.api.http.{Status, HeaderNames}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class Salesforce(implicit app: Application) {

  def ws(path: String): WSRequestHolder = {
    val auth = Auth(app.configuration.getString("salesforce.token").get, app.configuration.getString("salesforce.instance").get)

    WS.url(s"https://${auth.instance}/services/data/v32.0/sobjects/$path").
      withHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${auth.token}")
  }

  def upsertContact(user: User): Future[JsValue] = {

    val path = s"Contact/Twitter_Handle__c/${user.screen_name}"

    val json = Json.toJson(user)

    ws(path).patch(json).map { response =>
      response.status match {
        case Status.CREATED =>
          response.json
        case Status.NO_CONTENT =>
          Json.obj("success" -> true)
      }
    }
  }

  def createOrUpdateContacts(users: Seq[User]): Future[Seq[JsValue]] = {
    val uniqueUsers = users.groupBy(_.screen_name).mapValues(_.head).values
    Future.sequence {
      users.map { user =>
        upsertContact(user)
      }
    }
  }

  def deleteContact(user: User): Future[WSResponse] = {

    val path = s"Contact/Twitter_Handle__c/${user.screen_name}"

    val json = Json.toJson(user)

    ws(path).delete()
  }

  def upsertTweet(tweet: Tweet): Future[JsValue] = {

    val path = s"Tweet__c/Tweet_ID__c/${tweet.id}"

    val json = Json.toJson(tweet)

    ws(path).patch(json).map { response =>
      response.status match {
        case Status.CREATED =>
          response.json
        case Status.BAD_REQUEST =>
          Json.obj("success" -> true)
      }
    }

  }

  def upsertTweets(tweets: Seq[Tweet]) = {
    Future.sequence {
      tweets.map { tweet =>
        upsertTweet(tweet)
      }
    }
  }

}

object Salesforce {
  def apply(implicit app: Application) = new Salesforce()
}

case class Auth(token: String, instance: String)