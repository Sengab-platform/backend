package models

import play.api.libs.json.JsObject

case class User(
                 id: String,
                 entityType: String,
                 first_name: Option[String],
                 last_name: Option[String],
                 image: Option[String],
                 about: Option[JsObject],
                 stats: Map[String, Int],
                 created_at: String
               )
