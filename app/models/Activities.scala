package models

import models.project.Project.ActivityProject
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.language.postfixOps

case class Activities(
                       id: Int,
                       activity_type: String,
                       created_at: String,
                       project: ActivityProject
                     )

object Activities {

  lazy val req2db = (
    (__ \ "project").json.pickBranch(ActivityProject.rec2db) and
      coreReads
    ) reduce
  lazy val dp2resp = (
    (__ \ "project").json.pickBranch(ActivityProject.db2resp) and
      coreReads
    ) reduce
  implicit val dbFmt = Json.format[Activities]
  private val coreReads = (
    (__ \ "id").json.pickBranch and
      (__ \ "activity_type").json.pickBranch and
      (__ \ "created_at").json.pickBranch
    ) reduce
}