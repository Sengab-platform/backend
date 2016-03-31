package models.responses

import play.api.libs.json.Json

object UserResponses {

  case class About(
                    email: String,
                    bio: Option[String]
                  )

  case class Stats(
                    projects: Int,
                    contributions: Int
                  )

  case class UserInfoResponse(id: String,
                              url: String,
                              first_name: Option[String],
                              last_name: Option[String],
                              image: Option[String],
                              about: About,
                              stats: Stats,
                              projects: String,
                              contributions: String) extends UserResponse


  object About {
    implicit val f = Json.format[About]
  }

  object Stats {
    implicit val f = Json.format[Stats]
  }

  object UserInfoResponse {
    implicit val f = Json.format[UserInfoResponse]
  }

}