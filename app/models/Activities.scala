package models

import play.api.libs.json.Json

case class Project(
                    project_id: String,
                    project_name: String,
                    image: String)

object Project {
  implicit val f = Json.format[Project]
}

case class Activity(
                     activity_id: String,
                     activity_type: String,
                     created_at: String,
                     project: Project
                   )

object Activity {
  implicit val f = Json.format[Activity]

}

case class Activities(
                       id: String,
                       entity_type: String,
                       activities: Seq[Activity]
                     )

object Activities {
  implicit val f = Json.format[Activities]
}