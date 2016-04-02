package models

import play.api.libs.json.{JsValue, Json}

case class Contribution(
                         id: String,
                         url: String,
                         // TODO project ID ?? hmmm
                         projectID: String,
                         contributor: User,
                         created_at: String,
                         data: JsValue
                       )

object Contribution {
  implicit val ContributionF = Json.format[Contribution]

}