package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectDetails
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.DetailedProject
import utils.Constants


class ProjectRetrievementSpec extends AbstractSpec {


  // test Get Project Details Request

  "Receptionist Actor" should "Return details for project of template 1 successfully" in {
    receptionist ! GetProjectDetails(Constants.ProjectIDOfTemplate1)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProject].isSuccess)
  }

  it should "Return details for project of template 2 successfully" in {
    receptionist ! GetProjectDetails(Constants.ProjectIDOfTemplate2)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProject].isSuccess)
  }

  it should "Return details for project of template 3 successfully" in {
    receptionist ! GetProjectDetails(Constants.ProjectIDOfTemplate3)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProject].isSuccess)
  }

  it should "Return details for project of template 4 successfully" in {
    receptionist ! GetProjectDetails(Constants.ProjectIDOfTemplate4)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProject].isSuccess)
  }

  it should "Return NOT FOUND Error" in {
    receptionist ! GetProjectDetails(Constants.InvalidID)
    expectMsgType[NotFoundError]
  }

}
