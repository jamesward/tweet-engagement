package controllers

import play.api.Play
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.{Salesforce, Twitter}
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  val twitter = Twitter(Play.current)
  val salesforce = Salesforce(Play.current)

  def search(q: String) = Action.async {
    {
      for {
        tweets <- twitter.fetchOriginalTweets(q)
        users <- salesforce.createOrUpdateContacts(tweets.map(_.user))
        sentimentTweets <- twitter.sentimentForTweets(tweets)
        tweetsInSalesforce <- salesforce.upsertTweets(sentimentTweets)
        tasks <- salesforce.createTasksForTweets(tweetsInSalesforce, 50)
      } yield {
        Ok(
          Json.obj(
            "tweets" -> sentimentTweets,
            "tasks" -> tasks
          )
        )
      }
    } recover {
      case e: Exception => InternalServerError(e.getMessage)
    }
  }

}
