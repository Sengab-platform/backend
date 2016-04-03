package models

import play.api.libs.json.Json


case class EmbeddedCategory(category_id: String,
                            url: String,
                            name: String)


object EmbeddedCategory {
  implicit val f = Json.format[EmbeddedCategory]
}

case class DetailedCategory(
                             id: String,
                             url: String,
                             name: String,
                             image: String,
                             description: String
                           )

object DetailedCategory {
  implicit val f = Json.format[DetailedCategory]
}