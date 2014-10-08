package services


import models.User
import org.scalatestplus.play._
import play.api.test.Helpers._
import scala.concurrent.ExecutionContext.Implicits.global

class SalesforceSpec extends PlaySpec with OneAppPerSuite {



  "Salesforce" must {
    "upsert a contact" in {

      val user = User("foo bar", "foo")

      val json = await(Salesforce.upsertContact(user))

      (json \ "success").asOpt[Boolean] mustBe Some(true)
    }
    "upsert a contact again" in {

      val user = User("Foo Bar", "foo")

      val json = await(Salesforce.upsertContact(user))

      (json \ "success").asOpt[Boolean] mustBe Some(true)
    }
    "delete a contact" in {
      // todo: proper cleanup
      val user = User("Foo Bar", "foo")
      await(Salesforce.deleteContact(user))
    }
  }

}