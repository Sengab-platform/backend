package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectDetails
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.DetailedProject
import utils.Constants


class ProjectRetrievementSpec extends AbstractSpec {


  // test Get Project Details Request

  "Receptionist Actor" should "Return Project Details Successfully" in {
    receptionist ! GetProjectDetails(Constants.ValidProjectID)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProject].isSuccess)
  }

  it should "Return NOT FOUND Error" in {
    receptionist ! GetProjectDetails(Constants.InvalidID)
    expectMsgType[NotFoundError]
  }


}
