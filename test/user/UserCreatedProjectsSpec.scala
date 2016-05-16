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

  // User has not created any projects
  it should "Return NOT FOUND error as user has not created any projects" in {
    receptionist ! ListProjectsOfUser(Constants.UserIDWithNoCreatedProjects, Helper.CreatedKeyword, 0, 20)
    expectMsgType[NotFoundError]
  }

  // User not found
  it should "Return NOT FOUND error as the user is not existing" in {
    receptionist ! ListProjectsOfUser(Constants.InvalidID, Helper.CreatedKeyword, 0, 20)
    expectMsgType[NotFoundError]
  }
}
