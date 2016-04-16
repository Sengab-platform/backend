package models.contribution

import play.api.libs.json.Json

case class Contribution(
                         project_id: String,
                         created_at: String,
                         data: ContributionData
                       )

object Contribution {
  implicit val ContributionF = Json.format[Contribution]
}