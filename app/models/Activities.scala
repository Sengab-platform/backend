package models

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.language.postfixOps

case class Project(
                    id: String,
                    name: String,
                    url: String
                  )

object Project {

  lazy val rec2db = coreReads
  lazy val mongo2resp = (
    (__ \ "url").json.copyFrom((__ \ "id").json.pick) and
      coreReads reduce
    ) andThen genField
  implicit val f = Json.format[Project]
  private val coreReads = (
    (__ \ "id").json.pickBranch and
      (__ \ "name").json.pickBranch
    ) reduce

  private val genField = (__ \ "url").json.update(
    of[JsString].map { jsStr =>
      JsString(helpers.Helper.PROJECT_PATH + jsStr.value.trim)
    }
  )

}

case class Activities(
                       id: Int,
                       activity_type: String,
                       created_at: String,
                       project: Project
                     )

object Activities {

  lazy val req2mongo = (
    (__ \ "project").json.pickBranch(Project.rec2db) and
      coreReads
    ) reduce
  lazy val mongo2resp = (
    (__ \ "project").json.pickBranch(Project.mongo2resp) and
      coreReads
    ) reduce
  implicit val mongoFmt = Json.format[Activities]
  private val coreReads = (
    (__ \ "id").json.pickBranch and
      (__ \ "activity_type").json.pickBranch and
      (__ \ "created_at").json.pickBranch
    ) reduce
}