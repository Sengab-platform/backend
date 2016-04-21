package utils

import models.project.Project.NewProject
import models.project.Templates._

object Constants {

  val InvalidID = "invalid-id"

  val ValidUserID = "user::117521628211683444029"

  val ValidProjectID = "project::1"
  val NewProjectID = "project::5"

  val ValidProjectTemplateOne = NewProject(
    "Project of template 1",
    2000,
    "image",
    1,
    TemplateOne("What?"),
    "2015",
    "short",
    "detailed one",
    "category::1")

  val ValidProjectTemplateTwo = NewProject(
    "Project of template 2",
    2000,
    "image",
    2,
    TemplateTwo("Image title?"),
    "2015",
    "short",
    "detailed one",
    "category::5")

  val ValidProjectTemplateThree = NewProject(
    "Project of template 3",
    2000,
    "image",
    3,
    TemplateThree(List(
      new Question("1", "what?"), new Question("2", "why?"))),
    "2015",
    "short",
    "detailed one",
    "category::5")

  val ValidProjectTemplateFour = NewProject(
    "Project of template 4",
    2000,
    "image",
    4,
    TemplateFour("image title"),
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
