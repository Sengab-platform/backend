package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectStats
import models.errors.GeneralErrors.NotFoundError
import models.{Response, Stats}
import utils.Constants

class ProjectStatsRetrievement extends AbstractSpec {

  // The project has results
  "Receptionist Actor" should "Return stats for project of template 1 successfully" in {
    receptionist ! GetProjectStats(Constants.ProjectIDOfTemplate1)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Stats].isSuccess)
  }

  it should "Return stats for project of template 2 successfully" in {
    receptionist ! GetProjectStats(Constants.ProjectIDOfTemplate2)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Stats].isSuccess)
  }

  it should "Return stats for project of template 3 successfully" in {
    receptionist ! GetProjectStats(Constants.ProjectIDOfTemplate3)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Stats].isSuccess)
  }

  it should "Return stats for project of template 4 successfully" in {
    receptionist ! GetProjectStats(Constants.ProjectIDOfTemplate4)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Stats].isSuccess)
  }

  // no such project or initial state of the stats - for projects with no contributions
  it should "Return NOT FOUND project error" in {
    receptionist ! GetProjectStats(Constants.InvalidID)
    expectMsgType[NotFoundError]
  }

}
