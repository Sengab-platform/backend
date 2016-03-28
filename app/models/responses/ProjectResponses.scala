package models.responses

import play.api.libs.json.Json


object ProjectResponses extends ProjectResponse {

  case class CreateProjectResponse(id: String, url: String, name: String, image: String, created_at: String)

  object CreateProjectResponse {
    implicit val f = Json.format[CreateProjectResponse]
  }

}
