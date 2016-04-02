package models.project

import models.{EmbeddedCategory, EmbeddedOwner}
import play.api.libs.json.Json

object Project {

  case class NewProject(name: String,
                        goal: Int,
                        template_id: Int,
                        templateBody: TemplateBody,
                        created_at: String,
                        brief_description: String,
                        detailed_description: String,
                        is_featured: Boolean,
                        category_id: Int)

  case class DetailedProject(
                              id: String,
                              name: String,
                              owner: EmbeddedOwner,
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

  object NewProject {
    implicit val ىewProjectProjectF = Json.format[NewProject]
  }


  object DetailedProject {
    implicit val detailedProjectProjectF = Json.format[DetailedProject]
  }

}