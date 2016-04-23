package user

import core.AbstractSpec
import helpers.Helper
import messages.UserManagerMessages.ListProjectsOfUser
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.EmbeddedProject
import utils.Constants

class UserEnrolledProjectsSpec extends AbstractSpec {

  // User have enrolled in a project or more
  "Receptionist Actor" should "Get user enrolled projects Successfully" in {
    receptionist ! ListProjectsOfUser(Constants.ValidUserID, Helper.EnrolledKeyword, 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Seq[EmbeddedProject]].isSuccess)
  }

  // User has not enrolled in any projects
  it should "Return NOT FOUND error as user has not enrolled in any projects" in {
    receptionist ! ListProjectsOfUser(Constants.UserIDWithNoEnrolledProjects, Helper.EnrolledKeyword, 0, 20)
    expectMsgType[NotFoundError]
  }

  // User not found
  it should "Return NOT FOUND error as user is not existing" in {
    receptionist ! ListProjectsOfUser(Constants.InvalidID, Helper.EnrolledKeyword, 0, 20)
    expectMsgType[NotFoundError]
  }
}
