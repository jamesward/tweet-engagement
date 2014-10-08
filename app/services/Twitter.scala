package services

import models.Tweet
import play.api.Application
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSAuthScheme, WS}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Twitter(implicit app: Application) {

  val consumerKey = app.configuration.getString("twitter.consumer.key")
  val consumerSecret = app.configuration.getString("twitter.consumer.secret")

  lazy val bearerTokenFuture: Future[String] = {
    require(consumerKey.isDefined)
    require(consumerSecret.isDefined)

    WS.url("https://api.twitter.com/oauth2/token")
      .withAuth(consumerKey.get, consumerSecret.get, WSAuthScheme.BASIC)
      .post(Map("grant_type" ->  Seq("client_credentials")))
      .withFilter(response => (response.json \ "token_type").asOpt[String] == Some("bearer"))
      .map(response => (response.json \ "access_token").as[String])
  }

  def fetchTweets(query: String): Future[Seq[Tweet]] = {
    bearerTokenFuture.flatMap { bearerToken =>
      WS.url("https://api.twitter.com/1.1/search/tweets.json")
        .withQueryString("q" -> query)
        .withHeaders("Authorization" -> s"Bearer $bearerToken")
        .get()
        .map(_.json.\("statuses").as[Seq[Tweet]])
    }
  }

  def fetchOriginalTweets(query: String): Future[Seq[Tweet]] = {
    bearerTokenFuture.flatMap { bearerToken =>
      WS.url("https://api.twitter.com/1.1/search/tweets.json")
        .withQueryString("q" -> query)
        .withHeaders("Authorization" -> s"Bearer $bearerToken")
        .get()
        .map { response =>
          (response.json \ "statuses").as[Seq[JsValue]].map { tweetJson =>
            (tweetJson \ "retweeted_status").asOpt[Tweet].getOrElse(tweetJson.as[Tweet])
          }
        }
    }
  }

  def sentimentForTweets(tweets: Seq[Tweet]): Future[Seq[Tweet]] = {
    Future.sequence {
      tweets.map { tweet =>
        Sentiment.get(tweet.text).map { sentiment =>
          tweet.copy(sentiment = Some[Int](sentiment))
        }
      }
    }
  }

}

object Twitter {
  def apply(implicit app: Application) = new Twitter()
}