package models.results

import play.api.libs.json.Json

object Template3Results {

  case class Result(
                     id: Int,
                     title: String,
                     contributions_count: Int,
                     yes_count: Int,
                     no_count: Int
                   )

  case class Template3Results(
                               contributions_count: Int,
                               results: Seq[Result]
                             ) extends ProjectResult

  object Result {
    implicit val f = Json.format[Result]
  }

  object Template3Results {
    implicit val f = Json.format[Template3Results]
  }

}
