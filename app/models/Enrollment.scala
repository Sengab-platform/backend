package models

import play.api.libs.json.Json

case class Enrollment(projectID: String)

object Enrollment {
  implicit val EnrollmentF = Json.format[Enrollment]
}