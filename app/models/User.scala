package models

import play.api.libs.json.Json

case class About(
                  email: String,
                  bio: Option[String]
                )

object About {
  implicit val AboutF = Json.format[About]
}

case class Stats(
                  projects: Int,
                  contributions: Int
                )

object Stats {
  implicit val StatsF = Json.format[Stats]
}

case class User(id: String,
                url: String,
                first_name: Option[String],
                last_name: Option[String],
                image: Option[String],
                about: Option[About],
                stats: Option[Stats],
                projects: Option[String], // the User created projects url
                contributions: Option[String])

// the User contributions url

object User {
  implicit val UserF = Json.format[User]

  def generateEmbeddedOwner(id: String, first_name: String, image: String) =
    new User(id, helpers.Helper.USER_PATH + id, Some(first_name), None, Some(image), None, None, None, None)
}