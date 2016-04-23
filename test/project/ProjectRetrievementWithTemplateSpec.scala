package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectDetailsWithTemplateBody
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.DetailedProjectWithTemplateBody
import utils.Constants

class ProjectRetrievementWithTemplateSpec extends AbstractSpec {

  // valid project with template body
  "Receptionist Actor" should "Return details with template body of project 1 successfully" in {
    receptionist ! GetProjectDetailsWithTemplateBody(Constants.ProjectIDOfTemplate1)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProjectWithTemplateBody].isSuccess)
  }

  it should "Return details with template body of project 2 successfully" in {
    receptionist ! GetProjectDetailsWithTemplateBody(Constants.ProjectIDOfTemplate2)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProjectWithTemplateBody].isSuccess)
  }

  it should "Return details with template body of project 3 successfully" in {
    receptionist ! GetProjectDetailsWithTemplateBody(Constants.ProjectIDOfTemplate3)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProjectWithTemplateBody].isSuccess)
  }

  it should "Return details with template body of project 4 successfully" in {
    receptionist ! GetProjectDetailsWithTemplateBody(Constants.ProjectIDOfTemplate4)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProjectWithTemplateBody].isSuccess)
  }

  // no such project
  it should "Return NOT FOUND Error" in {
    receptionist ! GetProjectDetailsWithTemplateBody(Constants.InvalidID)
    expectMsgType[NotFoundError]
  }
}
