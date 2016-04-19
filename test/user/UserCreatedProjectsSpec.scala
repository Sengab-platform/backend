package user

import core.AbstractSpec
import helpers.Helper
import messages.UserManagerMessages.ListProjectsOfUser
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.EmbeddedProject

class UserCreatedProjectsSpec extends AbstractSpec {

  // User have created a project or more
  "Receptionist Actor" should "Get user created projects Successfully" in {
    receptionist ! ListProjectsOfUser("user::117521628211683444029", Helper.CreatedKeyword, 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Seq[EmbeddedProject]].isSuccess)
  }

  // User have not created any projects
  it should "Return NOT FOUND error" in {
    receptionist ! ListProjectsOfUser("invalid-id", Helper.CreatedKeyword, 0, 20)
    expectMsgType[NotFoundError]
  }
}
