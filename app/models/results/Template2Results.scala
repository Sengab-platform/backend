package models.results

import play.api.libs.json.Json

object Template2Results {

  case class Result(
                     image_url: String,
                     caption: String
                   )

  case class Template2Results(
                               contributions_count: Int,
                               results: Seq[Result]
                             ) extends ProjectResult

  object Result {
    implicit val f = Json.format[Result]
  }

  object Template2Results {
    implicit val f = Json.format[Template2Results]
  }

}