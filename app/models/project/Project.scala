package models.project

import play.api.libs.json.Json

case class Project(name: String,
                   brief_description: String,
                   detailed_description: String,
                   goal: Int,
                   is_featured: Boolean,
                   created_at: String,
                   category_id: Int,
                   template_id: Int,
                   templateBody: TemplateBody) {
}

object Project {
  implicit val ProjectF = Json.format[Project]

}
