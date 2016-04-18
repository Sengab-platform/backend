package models.results

import play.api.libs.json.Json

object Template1Results {

  case class location(
                       lat: Double,
                       lng: Double
                     )

  case class Results(
                      yes: Seq[location],
                      no: Seq[location]
                    )

  case class Template1Results(
                               contributions_count: Int,
                               results: Results
                             ) extends ProjectResult {

  }

  object location {
    implicit val f = Json.format[location]
  }

  object Results {
    implicit val f = Json.format[Results]
  }

  object Template1Results {
    implicit val f = Json.format[Template1Results]
  }

}