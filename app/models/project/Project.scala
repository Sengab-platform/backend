package models.project

import models.Category
import play.api.libs.json.Json

case class Owner(
                  id: String,
                  url: String,
                  name: String
                )

object Owner {
  implicit val OwnerF = Json.format[Owner]

}

case class Project(
                    id: Option[String] = None,
                    url: Option[String] = None,
                    name: String,
                    image: String,
                    owner: Option[Owner] = None,
                    goal: Int,
                    template_id: Option[Int] = None,
                    templateBody: Option[TemplateBody] = None,
                    created_at: String,
                    brief_description: String,
                    detailed_description: String,
                    enrollments_count: Option[String] = None,
                    contributions_count: Option[String] = None,
                    is_featured: Option[Boolean] = None,
                    category_id: Option[Int] = None,
                    category: Category,
                    results: String,
                    stats: String
                  )

object Project {
  implicit val ProjectF = Json.format[Project]

}