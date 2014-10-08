package services

import models.User
import org.scalatestplus.play._
import org.scalatest.matchers.BeMatcher._
import play.api.test.Helpers._

class TwitterSpec extends PlaySpec with OneAppPerSuite {

  "Twitter" must {
    "query" in {
      val tweets = await(Twitter(app).fetchTweets("@_jamesward"))
      tweets mustNot have length 0
    }
    "query original tweets" in {
      val tweets = await(Twitter(app).fetchOriginalTweets("@_jamesward"))
      all (tweets.map(_.text)) mustNot startWith ("RT")
    }
  }

}