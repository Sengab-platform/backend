package models.project

import models.{Category, EmbeddedCategory, User}
import play.api.libs.json.Json

class Project(
               id: Option[String] = None,
               url: Option[String] = None,
               name: String,
               owner: Option[User] = None,
               goal: Int,
               template_id: Option[Int] = None,
               templateBody: Option[TemplateBody] = None,
               created_at: String,
               brief_description: String,
               detailed_description: String,
               enrollments_count: Option[Int] = None,
               contributions_count: Option[Int] = None,
               is_featured: Option[Boolean] = None,
               category_id: Option[Int] = None,
               category: Option[Category],
               results: Option[String],
               stats: Option[String]
             )

object Project {
  implicit val ProjectF = Json.format[Project]
}


case class NewProject(name: String,
                      owner: User,
                      goal: Int,
                      template_id: Int,
                      templateBody: TemplateBody,
                      created_at: String,
                      brief_description: String,
                      detailed_description: String,
                      is_featured: Boolean,
                      category_id: Int) extends Project(None, None, name, Some(owner), goal, Some(template_id), Some(templateBody), created_at, brief_description, detailed_description, Some(0), Some(0), Some(is_featured), Some(category_id), None, None, None)


case class DetailedProject(
                            id: String,
                            name: String,
                            owner: User,
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
                          ) extends Project(Some(id), Some(helpers.Helper.PROJECT_PATH + id), name, Some(owner), goal, Some(template_id), None, created_at, brief_description, detailed_description, Some(enrollments_count), Some(contributions_count), Some(is_featured), None, Some(category), Some(helpers.Helper.RESULT_PATH + results), Some(helpers.Helper.STAT_PATH + stats))


