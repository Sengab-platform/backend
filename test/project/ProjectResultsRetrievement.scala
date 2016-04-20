package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectResults
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.results.ProjectResult
import utils.Constants

class ProjectResultsRetrievement extends AbstractSpec {

  // The project has results
  "Receptionist Actor" should "Return Project Results Successfully" in {
    receptionist ! GetProjectResults(Constants.ValidProjectID, 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[ProjectResult].isSuccess)
  }

  // no such project
  it should "Return NOT FOUND project error" in {
    receptionist ! GetProjectResults(Constants.InvalidID, 0, 20)
    expectMsgType[NotFoundError]
  }

  // initial state of the results - for projects with no contributions
  it should "Return NOT FOUND results error" in {
    receptionist ! GetProjectResults(Constants.NewProjectID, 0, 20)
    expectMsgType[NotFoundError]
  }
}
