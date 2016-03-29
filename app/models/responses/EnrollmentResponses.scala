package models.responses

import models.project.TemplateBody
import play.api.libs.json.Json


object EnrollmentResponses extends EnrollmentResponse {

  case class EnrollResponse(id: String, url: String, project_id: String, template_id: Int, template_body: TemplateBody)

  case class WithdrawResponse(id: String, url: String)

  object EnrollResponse {
    implicit val EnrollRes = Json.format[EnrollResponse]
  }

  object WithdrawResponse {
    implicit val WithdrawRes = Json.format[WithdrawResponse]
  }

}
