package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectResults
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.results.ProjectResult

class ProjectResultsRetrievement extends AbstractSpec {

  // The project has results
  "Receptionist Actor" should "Return Project Results Successfully" in {
    receptionist ! GetProjectResults("project::1", 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[ProjectResult].isSuccess)
  }

  // no such project
  it should "Return NOT FOUND project error" in {
    receptionist ! GetProjectResults("invalid-projectID", 0, 20)
    expectMsgType[NotFoundError]
  }

  // initial state of the results - for projects with no contributions
  it should "Return NOT FOUND results error" in {
    receptionist ! GetProjectResults("project::13c11220-251a-4cd6-992b-98dab9d2e650", 0, 20)
    expectMsgType[NotFoundError]
  }
}
