package models

import play.api.libs.json.Json

case class Project(
                    id: String,
                    name: String,
                    url: Option[String] = None)

object Project {
  implicit val f = Json.format[Project]
}

case class Activities(
                       id: Int,
                       activity_type: String,
                       created_at: String,
                       project: Project
                     )

object Activities {
  implicit val f = Json.format[Activities]
}