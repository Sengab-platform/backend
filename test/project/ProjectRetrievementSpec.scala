package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectDetails
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.DetailedProject


class ProjectRetrievementSpec extends AbstractSpec {


  // test Get Project Details Request

  "Receptionist Actor" should "Return Project Details Successfully" in {
    receptionist ! GetProjectDetails("project::01758af6-9a5b-4a88-8b40-77c98cdf87d7")
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[DetailedProject].isSuccess)
  }

  it should "Return NOT FOUND Error" in {
    receptionist ! GetProjectDetails("invalid-projectID")
    expectMsgType[NotFoundError]
  }


}
