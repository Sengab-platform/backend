package models

import play.api.libs.json.{JsObject, Json}

case class User(
                 id: String,
                 entityType: String,
                 first_name: Option[String],
                 last_name: Option[String],
                 image: Option[String],
                 about: Option[JsObject],
                 stats: Map[String, Int],
                 enrolled_projects: Option[List[String]] = None,
                 created_at: String
               )

object User {
  implicit val user = Json.format[User]
}