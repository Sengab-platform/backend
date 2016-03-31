package models.responses

import play.api.libs.json.Json

object UserResponses {

  case class UserInfoResponse(id: String,
                              url: String,
                              first_name: Option[String],
                              last_name: Option[String],
                              image: Option[String],
                              about: about,
                              stats: stats,
                              projects: String,
                              contributions: String) extends UserResponse

  case class about(
                    email: String,
                    bio: Option[String]
                  )

  case class stats(
                    projects: Int,
                    contributions: Int
                  )


  object UserInfoResponse {
    implicit val f2 = Json.format[about]
    implicit val f1 = Json.format[stats]
    implicit val f = Json.format[UserInfoResponse]
  }

  object about {
    implicit val f = Json.format[about]
  }

  object stats {
    implicit val f = Json.format[stats]
  }

}