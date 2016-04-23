package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectResults
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.results.ProjectResult
import utils.Constants

class ProjectResultsRetrievement extends AbstractSpec {

  // The project has results
  "Receptionist Actor" should "Return results for template 1 projects successfully" in {
    receptionist ! GetProjectResults(Constants.ProjectIDOfTemplate1, 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[ProjectResult].isSuccess)
  }

  it should "Return results for template 2 projects successfully" in {
    receptionist ! GetProjectResults(Constants.ProjectIDOfTemplate2, 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[ProjectResult].isSuccess)
  }

  it should "Return results for template 3 projects successfully" in {
    receptionist ! GetProjectResults(Constants.ProjectIDOfTemplate3, 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[ProjectResult].isSuccess)
  }

  it should "Return results for template 4 projects successfully" in {
    receptionist ! GetProjectResults(Constants.ProjectIDOfTemplate4, 0, 20)
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
