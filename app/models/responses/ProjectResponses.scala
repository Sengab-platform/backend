package models.responses

import play.api.libs.json.Json


object ProjectResponses {

  case class CreateProjectResponse(id: String, url: String, name: String, created_at: String)
    extends ProjectResponse

  case class ProjectDetailsResponse(id: String,
                                    url: String,
                                    name: String,
                                    created_at: String,
                                    brief_description: String,
                                    detailed_description: String,
                                    is_featured: Boolean,
                                    results: String,
                                    stats: String
                                   ) extends ProjectResponse

  object CreateProjectResponse {
    implicit val f = Json.format[CreateProjectResponse]
  }

  object ProjectDetailsResponse {
    implicit val f = Json.format[ProjectDetailsResponse]
  }


}
