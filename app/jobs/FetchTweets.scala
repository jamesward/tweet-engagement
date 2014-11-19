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

  val twitter = Twitter(app)
  val salesforce = Salesforce(app)

  val job = for {
    tweets             <- twitter.fetchOriginalTweets("#SalesforceTour")
    users              <- salesforce.createOrUpdateContacts(tweets.map(_.user))
    sentimentTweets    <- twitter.sentimentForTweets(tweets)
    tweetsInSalesforce <- salesforce.upsertTweets(sentimentTweets)
    tasks              <- salesforce.createTasksForTweets(tweetsInSalesforce, 50)
  } yield tasks

  job.onComplete {
    case Success(s) =>
      Play.stop()
    case Failure(e) =>
      Logger.error(e.getMessage)
      Play.stop()
  }

}
