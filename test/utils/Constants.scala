package utils

import models.Contributor
import models.contribution.Contribution
import models.contribution.ContributionDataTypes._
import models.project.Project.NewProject
import models.project.Templates._

object Constants {

  val InvalidID = "invalid-id"

  val ValidUserID = "user::117521628211683444029"
  val UserIDWithNoActivity = "user::1"
  val UserIDWithNoCreatedProjects = UserIDWithNoActivity
  val UserIDWithNoEnrolledProjects = UserIDWithNoActivity


  val ProjectIDOfTemplate1 = "project::1"
  val ProjectIDOfTemplate2 = "project::2"
  val ProjectIDOfTemplate3 = "project::3"
  val ProjectIDOfTemplate4 = "project::4"

  val NewProjectID = "project::7"

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

  val ValidSearchKeyword = "project"
  val InvalidSearchKeyword = "dummytexttoinsureitwillfail"

  val ValidProjectToEnroll = "project::5"
  val AlreadyEnrolledProject = ProjectIDOfTemplate1

  val ValidCategoryID = "category::1"
  val InvalidCategoryID = "category::100"


  val Contributor = new Contributor(ValidUserID, "male")
  val location = new Location(33, 44)

  val ContributionTemplateOne = new Contribution(
    "project::1",
    "2016",
    new ContributionDataTypeOne(location, "answer")
  )

  val ContributionTemplateTwo = new Contribution(
    "project::2",
    "2016",
    new ContributionDataTypeTwo("image", "caption")
  )

  val ContributionTemplateThree = new Contribution(
    "project::3",
    "2016",
    new ContributionDataTypeThree(Seq(new Answer(1, "answer"), new Answer(2, "answer"))
    ))

  val ContributionTemplateFour = new Contribution(
    "project::4",
    "2016",
    new ContributionDataTypeFour("image", "caption", location)
  )

}
