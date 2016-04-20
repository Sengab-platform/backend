package project

import core.AbstractSpec
import messages.ProjectManagerMessages.GetProjectStats
import models.errors.GeneralErrors.NotFoundError
import models.{Response, Stats}

class ProjectStatsRetrievement extends AbstractSpec {
  // The project has results
  "Receptionist Actor" should "Return Project Stats Successfully" in {
    receptionist ! GetProjectStats("project::1")
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Stats].isSuccess)
  }

  // no such project or initial state of the stats - for projects with no contributions
  it should "Return NOT FOUND project error" in {
    receptionist ! GetProjectStats("invalid-projectID")
    expectMsgType[NotFoundError]
  }

}
