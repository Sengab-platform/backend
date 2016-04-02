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
                projects: String,
                contributions: String)

object User {
  implicit val UserF = Json.format[User]
}