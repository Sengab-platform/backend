package models

import play.api.libs.json.Json

case class About(
                  email: Option[String],
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

case class User(
                 id: String,
                 url: String,
                 first_name: Option[String],
                 last_name: Option[String],
                 image: Option[String],
                 about: Option[About],
                 stats: Option[Stats],
                 projects: Option[String], // the User created projects url
                 contributions: Option[String], // the User contributions url
                 created_at: String
               )

object User {
  implicit val UserF = Json.format[User]
}

case class NewUser(
                    id: String,
                    entity_type: String = "user",
                    first_name: Option[String],
                    last_name: Option[String],
                    image: Option[String],
                    about: Option[About],
                    stats: Option[Stats],
                    created_at: String,
                    contributions: String,
                    projects: String,
                    enrolled_projects: List[String]
                  )

object NewUser {
  implicit val NewUserF = Json.format[NewUser]
}

case class EmbeddedOwner(id: String, url: String, name: String, image: String)

object EmbeddedOwner {
  implicit val embeddedOwnerF = Json.format[EmbeddedOwner]
}

case class UserInfo(
                     id: String,
                     first_name: Option[String],
                     last_name: Option[String],
                     image: Option[String],
                     about: Option[About],
                     stats: Stats,
                     projects: String, // the User created projects url
                     contributions: String, // the User created contributions url
                     url: String
                   )

object UserInfo {
  implicit val UserF = Json.format[UserInfo]
}