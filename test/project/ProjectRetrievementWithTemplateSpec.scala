package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectDetailsWithTemplateBody
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.DetailedProjectWithTemplateBody
import utils.Constants

class ProjectRetrievementWithTemplateSpec extends AbstractSpec {

  // valid project with template body
  "Receptionist Actor" should "Return Project Details with Template Body Successfully" in {
    receptionist ! GetProjectDetailsWithTemplateBody(Constants.ValidProjectID)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProjectWithTemplateBody].isSuccess)
  }

  // no such project
  it should "Return NOT FOUND Error" in {
    receptionist ! GetProjectDetailsWithTemplateBody(Constants.InvalidID)
    expectMsgType[NotFoundError]
  }
}
