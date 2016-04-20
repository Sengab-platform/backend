package project

import core.AbstractSpec
import messages.ProjectManagerMessages.SearchProjects
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.EmbeddedProject
import utils.Constants

class ProjectSearchSpec extends AbstractSpec {

  "Receptionist Actor" should "Return Projects with `recognize` in their titles Successfully" in {
    receptionist ! SearchProjects(Constants.ValidSearchKeyword, 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Seq[EmbeddedProject]].isSuccess)
  }

  it should "Return NOT FOUND Error" in {
    receptionist ! SearchProjects(Constants.InvalidSearchKeyword, 0, 20)
    expectMsgType[NotFoundError]
  }

}
