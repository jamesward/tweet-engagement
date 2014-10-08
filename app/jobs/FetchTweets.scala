package jobs

import java.io.File

import models.Tweet
import play.api._
import play.api.libs.ws.WS
import services.{Sentiment, Salesforce, Twitter}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}

object FetchTweets extends App {

  implicit val app = new DefaultApplication(new File("."), FetchTweets.getClass.getClassLoader, null, Mode.Prod)

  Play.start(app)

  val job = for {
    tweets             <- Twitter(app).fetchOriginalTweets("#df14")
    users              <- Salesforce(app).createOrUpdateContacts(tweets.map(_.user))
    sentimentTweets    <- Twitter(app).sentimentForTweets(tweets)
    tweetsInSalesforce <- Salesforce(app).upsertTweets(sentimentTweets)
  } yield tweetsInSalesforce

  job.onComplete {
    case Success(s) =>
      Play.stop()
    case Failure(e) =>
      Logger.error(e.getMessage)
      Play.stop()
  }

}
