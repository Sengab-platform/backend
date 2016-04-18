package models.results

import models.results.Template1Results.location
import play.api.libs.json.Json

object Template4Results {

  case class Result(
                     image_url: String,
                     caption: String,
                     location: location
                   )

  case class Template4Results(
                               contributions_count: Int,
                               results: Seq[Result]
                             ) extends ProjectResult

  object Result {
    implicit val f = Json.format[Result]
  }

  object Template4Results {
    implicit val f = Json.format[Template4Results]
  }

}