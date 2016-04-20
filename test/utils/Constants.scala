package utils

import models.project.Project.NewProject
import models.project.Templates.TemplateOne

object Constants {

  val ValidUserID = "user::117521628211683444029"
  val InvalidID = "invalid-id"

  val ValidProjectID = "project::1"
  val NewProjectID = "project::5"
  val ValidProject = NewProject("Recognize",
    2000,
    "image",
    1,
    TemplateOne("What?"),
    "2015",
    "short",
    "detailed one",
    "category::5")
  val ValidSearchKeyword = "recognize"
  val InvalidSearchKeyword = "dummytexttoinsureitwillfail"
  val ValidProjectToEnroll = "project::2"
  val AlreadyEnrolledProject = ValidProjectID

  val ValidCategoryID = "category::1"
  val InvalidCategoryID = "category::100"

}
