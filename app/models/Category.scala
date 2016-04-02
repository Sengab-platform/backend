package models

import play.api.libs.json.Json

case class Category(
                     id: String,
                     name: String,
                     url: String,
                     image: Option[String] = None,
                     description: Option[String] = None
                   )

object Category {
  implicit val CategoryF = Json.format[Category]

}