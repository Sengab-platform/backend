package models

import models.project.Project
import play.api.libs.json.{JsValue, Json}

case class Contribution(
                         id: String,
                         url: String,
                         project: Project,
                         contributor: User,
                         created_at: String,
                         data: JsValue
                       )

object Contribution {
  implicit val ContributionF = Json.format[Contribution]

}