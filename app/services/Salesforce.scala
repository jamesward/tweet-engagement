package services

import models.{Tweet, User}
import play.api.Application
import play.api.libs.json.{JsObject, Json, JsValue}
import play.api.libs.ws.{WSResponse, WSRequestHolder, WS}
import play.api.http.{Status, HeaderNames}
import play.api.mvc.Results.EmptyContent
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class Salesforce(implicit app: Application) {

  lazy val authFuture: Future[Auth] = {
    val params = Map(
      "grant_type" -> "password",
      "client_id" -> app.configuration.getString("salesforce.consumer.key").get,
      "client_secret" -> app.configuration.getString("salesforce.consumer.secret").get,
      "username" -> app.configuration.getString("salesforce.username").get,
      "password" -> app.configuration.getString("salesforce.password").get
    )

    WS.
      url("https://login.salesforce.com/services/oauth2/token").
      withQueryString(params.toSeq:_*).
      post(EmptyContent()).
      flatMap { response =>
        response.status match {
          case Status.OK =>
            Future.successful(Auth((response.json \ "access_token").as[String], (response.json \ "instance_url").as[String]))
          case _ =>
            Future.failed(new IllegalStateException(s"Auth Denied: ${response.body}"))
        }
      }
  }


  def ws(path: String): Future[WSRequestHolder] = {
    authFuture.map { auth =>
      WS.
        url(s"${auth.instance}/services/data/v32.0/sobjects/$path").
        withHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${auth.token}")
    }
  }

  def upsertContact(user: User): Future[JsValue] = {

    val path = s"Contact/Twitter_Handle__c/${user.screen_name}"

    val json = Json.toJson(user)

    ws(path).flatMap {
      _.patch(json).map { response =>
        response.status match {
          case Status.CREATED =>
            response.json
          case Status.NO_CONTENT =>
            Json.obj("success" -> true)
        }
      }
    }
  }

  def createOrUpdateContacts(users: Seq[User]): Future[Seq[JsValue]] = {
    val uniqueUsers = users.groupBy(_.screen_name).mapValues(_.head).values.toSeq
    Future.sequence {
      uniqueUsers.map { user =>
        upsertContact(user)
      }
    }
  }

  def deleteContact(user: User): Future[WSResponse] = {
    val path = s"Contact/Twitter_Handle__c/${user.screen_name}"

    ws(path).flatMap(_.delete())
  }

  def upsertTweet(tweet: Tweet): Future[Tweet] = {

    val path = s"Tweet__c/Tweet_ID__c/${tweet.id}"

    val json = Json.toJson(tweet)

    ws(path).flatMap {
      _.patch(json).map { response =>
        tweet.copy(salesforceId = (response.json \ "id").asOpt[String])
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

  def getContact(userName: String): Future[JsValue] = {
    val path = s"Contact/Twitter_Handle__c/$userName"

    ws(path).flatMap(_.get().map(_.json))
  }

  def createTaskForTweet(tweet: Tweet): Future[JsValue] = {

    getContact(tweet.user.screen_name).flatMap { contactJson =>

      val contactId = (contactJson \ "Id").as[String]

      val path = s"Task"

      val json = Json.obj(
        "Priority" -> "Normal",
        "Status" -> "Not Started",
        "Subject" -> "Investigate Tweet",
        "WhoId" -> contactId
      )

      ws(path).flatMap(_.post(json).map(_.json))
    }

  }

  def createTasksForTweets(tweets: Seq[Tweet], maxSentiment: Int): Future[Seq[JsValue]] = {
    Future.sequence {
      tweets.
        filter(_.salesforceId.isDefined).
        filter(_.sentiment.exists(_ < maxSentiment)).
        map(createTaskForTweet)
    }
  }

}

object Salesforce {
  def apply(implicit app: Application) = new Salesforce()
}

case class Auth(token: String, instance: String)