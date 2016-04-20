package user

import core.AbstractSpec
import helpers.Helper
import messages.UserManagerMessages.ListProjectsOfUser
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.EmbeddedProject
import utils.Constants

class UserCreatedProjectsSpec extends AbstractSpec {

  // User have created a project or more
  "Receptionist Actor" should "Get user created projects Successfully" in {
    receptionist ! ListProjectsOfUser(Constants.ValidUserID, Helper.CreatedKeyword, 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Seq[EmbeddedProject]].isSuccess)
  }

  // User have not created any projects
  it should "Return NOT FOUND error" in {
    receptionist ! ListProjectsOfUser(Constants.InvalidID, Helper.CreatedKeyword, 0, 20)
    expectMsgType[NotFoundError]
  }
}
