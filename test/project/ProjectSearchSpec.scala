package project

import core.AbstractSpec
import messages.ProjectManagerMessages.SearchProjects
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.EmbeddedProject

class ProjectSearchSpec extends AbstractSpec {

  "Receptionist Actor" should "Return Projects with `recognize` in their titles Successfully" in {
    receptionist ! SearchProjects("recognize", 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Seq[EmbeddedProject]].isSuccess)
  }

  it should "Return NOT FOUND Error" in {
    receptionist ! SearchProjects("invalid-projectID", 0, 20)
    expectMsgType[NotFoundError]
  }

}
