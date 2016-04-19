package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectDetailsWithTemplateBody
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.DetailedProjectWithTemplateBody

class ProjectRetrievementWithTemplateSpec extends AbstractSpec {

  // valid project with template body
  "Receptionist Actor" should "Return Project Details with Template Body Successfully" in {
    receptionist ! GetProjectDetailsWithTemplateBody("project::01758af6-9a5b-4a88-8b40-77c98cdf87d7")
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProjectWithTemplateBody].isSuccess)
  }

  // no such project
  it should "Return NOT FOUND Error" in {
    receptionist ! GetProjectDetailsWithTemplateBody("invalid-projectID")
    expectMsgType[NotFoundError]
  }
}
