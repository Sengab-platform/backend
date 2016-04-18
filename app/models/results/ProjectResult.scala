package models.results

import models.results.Template1Results.Template1Results
import models.results.Template2Results.Template2Results
import models.results.Template3Results.Template3Results
import models.results.Template4Results.Template4Results
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, Json, Reads, Writes}

trait ProjectResult

object ProjectResult {

  implicit val tempResultR: Reads[ProjectResult] =
    Json.format[Template1Results].map(x => x: ProjectResult) or
      Json.format[Template2Results].map(x => x: ProjectResult) or
      Json.format[Template3Results].map(x => x: ProjectResult) or
      Json.format[Template4Results].map(x => x: ProjectResult)


  implicit val tempResultW = new Writes[ProjectResult] {
    def writes(projectResult: ProjectResult): JsValue = {
      projectResult match {
        case m: Template1Results => Json.toJson(m)
        case m: Template2Results => Json.toJson(m)
        case m: Template3Results => Json.toJson(m)
        case m: Template4Results => Json.toJson(m)
        case _ => Json.obj("error" -> "wrong Json")
      }
    }
  }

}