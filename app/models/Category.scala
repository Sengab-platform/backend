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


  /**
    * Use when getting the category embedded in a project
    *
    * @return
    */
  def generateEmbeddedCategory(id: String,
                               name: String) = Category(id, name, helpers.Helper.CATEGORY_PATH + id, None, None)


  def generateDetailedCategory(
                                id: String,
                                name: String,
                                image: String,
                                description: String
                              ) = Category(id, name, helpers.Helper.CATEGORY_PATH + id, Some(image), Some(description))
}