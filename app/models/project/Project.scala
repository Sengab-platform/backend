package models.project

import models.{EmbeddedCategory, EmbeddedOwner}
import play.api.libs.json.Json

object Project {

  case class NewProject(name: String,
                        goal: Int,
                        image: String,
                        template_id: Int,
                        template_body: TemplateBody,
                        created_at: String,
                        brief_description: String,
                        detailed_description: String,
                        category_id: String)

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
                              enrollments_count: Option[Int],
                              contributions_count: Option[Int],
                              category: EmbeddedCategory,
                              results: String,
                              stats: String
                            )

  case class DetailedProjectWithTemplateBody(
                                              id: String,
                                              name: String,
                                              owner: EmbeddedOwner,
                                              url: String,
                                              goal: Int,
                                              image: String,
                                              template_id: Int,
                                              template_body: TemplateBody,
                                              created_at: String,
                                              brief_description: String,
                                              detailed_description: String,
                                              enrollments_count: Option[Int],
                                              contributions_count: Option[Int],
                                              category: EmbeddedCategory,
                                              results: String,
                                              stats: String
                                            )

  case class EmbeddedProject(
                              id: String,
                              name: String,
                              owner: EmbeddedOwner,
                              url: String,
                              goal: Int,
                              image: String,
                              template_id: Int,
                              created_at: String,
                              brief_description: String,
                              enrollments_count: Option[Int],
                              contributions_count: Option[Int],
                              category: EmbeddedCategory
                            )

  object NewProject {
    implicit val newProjectProjectF = Json.format[NewProject]
  }


  object DetailedProject {
    implicit val detailedProjectProjectF = Json.format[DetailedProject]
  }

  object DetailedProjectWithTemplateBody {
    implicit val detailedProjectProjectF = Json.format[DetailedProjectWithTemplateBody]
  }

  object EmbeddedProject {
    implicit val EmbeddedProjectF = Json.format[EmbeddedProject]
  }

}