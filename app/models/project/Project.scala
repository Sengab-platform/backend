package models.project

import models.{EmbeddedCategory, User}
import play.api.libs.json.Json

case class NewProject(name: String,
                      owner: User,
                      goal: Int,
                      template_id: Int,
                      templateBody: TemplateBody,
                      created_at: String,
                      brief_description: String,
                      detailed_description: String,
                      is_featured: Boolean,
                      category_id: Int)

object NewProject {
  implicit val f = Json.format[NewProject]
}

case class DetailedProject(
                            id: String,
                            name: String,
                            owner: User,
                            url: String,
                            goal: Int,
                            image: String,
                            template_id: Int,
                            created_at: String,
                            brief_description: String,
                            detailed_description: String,
                            enrollments_count: Int,
                            contributions_count: Int,
                            is_featured: Boolean,
                            category: EmbeddedCategory,
                            results: String,
                            stats: String
                          )


object DetailedProject {
  implicit val f = Json.format[DetailedProject]
}