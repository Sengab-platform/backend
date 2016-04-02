package models.responses

import play.api.libs.json.Json

object ActivityResponse {

  case class Project(
                      id: String,
                      name: String,
                      image: String,
                      url: String
                    )

  case class Activities(
                         id: String,
                         project: Project,
                         created_at: String,
                         activity_type: String
                       )

  case class UserActivityResponse(
                                   activities: Seq[Activities]
                                 ) extends UserResponse

  object Project {
    implicit val f = Json.format[Project]
  }

  object Activities {
    implicit val f = Json.format[Activities]
  }

  object UserActivityResponse {
    implicit val f = Json.format[UserActivityResponse]
  }

}