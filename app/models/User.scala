package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class User(name: String, screen_name: String)

object User {

  // converts from Twitter JSON to a User
  implicit val twitterReads: Reads[User] = Json.reads[User]

  // converts from User to Salesforce JSON
  implicit val salesforceWrites: Writes[User] = Json.writes[User].transform { user =>
    val fullName = (user \ "name").as[String].split(" ").reverse

    val lastName = fullName.head
    val firstName = fullName.tail.reverse.mkString(" ")

    Json.obj("FirstName" -> firstName, "LastName" -> lastName)
  }

}