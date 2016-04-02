package models

import play.api.libs.json.Json

class Category(
                id: String,
                name: String,
                url: String,
                image: Option[String] = None,
                description: Option[String] = None
              )

object Category {
  implicit val CategoryF = Json.format[Category]
}


case class EmbeddedCategory(id: String,
                            name: String) extends Category(id, name, helpers.Helper.CATEGORY_PATH + id, None, None)


case class DetailedCategory(
                             id: String,
                             name: String,
                             image: String,
                             description: String
                           ) extends Category(id, name, helpers.Helper.CATEGORY_PATH + id, Some(image), Some(description))
