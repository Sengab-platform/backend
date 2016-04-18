package models.project

import models.{EmbeddedCategory, EmbeddedOwner}

import scala.language.postfixOps

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

  import helpers.Helper
  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  import scala.language.postfixOps

  case class ActivityProject(
                              id: String,
                              name: String,
                              url: String
                            )

  object ActivityProject {

    lazy val rec2db = coreReads
    lazy val db2resp = (
      (__ \ "url").json.copyFrom((__ \ "id").json.pick) and
        coreReads reduce
      ) andThen genField
    implicit val f = Json.format[ActivityProject]
    private val coreReads = (
      (__ \ "id").json.pickBranch and
        (__ \ "name").json.pickBranch
      ) reduce

    private val genField = (__ \ "url").json.update(
      of[JsString].map { jsStr =>
        JsString(Helper.ProjectPath + jsStr.value.trim)
      }
    )

  }
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