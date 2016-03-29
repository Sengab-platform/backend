package models.enrollment

import play.api.libs.json.Json

case class Enrollment(user_id: String, project_id: String)

object Enrollment {
  implicit val enrollment = Json.format[Enrollment]
}
