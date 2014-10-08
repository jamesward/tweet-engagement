package models

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Tweet(id: String, text: String, user: User, date: Date, sentiment: Option[Int] = None)

object Tweet {

  implicit val twitterDateReads: Reads[Date] = Reads.dateReads("EEE MMM dd HH:mm:ss ZZZZZ yyyy")

  implicit val twitterReads: Reads[Tweet] = (
    (__ \ "id_str").read[String] ~
    (__ \ "text").read[String] ~
    (__ \ "user").read[User] ~
    (__ \ "created_at").read[Date] ~
    Reads.pure(None)
  )(Tweet.apply _)

  implicit val salesforceWrites: Writes[Tweet] = (
    (__ \ "Name").write[String] ~
    (__ \ "Text__c").write[String] ~
    (__ \ "Contact__r" \ "Twitter_Handle__c").write[String] ~
    (__ \ "Date__c").write[Date] ~
    (__ \ "Sentiment__c").writeNullable[Int]
  ) { tweet =>
    val df = new SimpleDateFormat("MM/dd/yyyy")
    val date = df.format(tweet.date)
    val summary = s"${tweet.sentiment.get}% Positive Tweet by ${tweet.user.name} on $date"
    (summary, tweet.text, tweet.user.screen_name, tweet.date, tweet.sentiment)
  }

}