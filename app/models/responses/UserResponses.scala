package models.responses

import play.api.libs.json.{JsObject, Json}

object UserResponses {

  case class UserInfoResponse(id: String,
                              url: String,
                              first_name: Option[String],
                              last_name: Option[String],
                              image: Option[String],
                              about: JsObject,
                              stats: JsObject,
                              projects: String,
                              contributions: String) extends UserResponse

  object UserInfoResponse {
    implicit val f = Json.format[UserInfoResponse]
  }

}
