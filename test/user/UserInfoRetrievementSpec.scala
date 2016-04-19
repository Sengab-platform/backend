package user

import core.AbstractSpec
import messages.UserManagerMessages.GetUserProfile
import models.errors.GeneralErrors.NotFoundError
import models.{Response, UserInfo}

class UserInfoRetrievementSpec extends AbstractSpec {

  "Receptionist Actor" should "Get User Info Successfully" in {
    receptionist ! GetUserProfile("user::117521628211683444029")
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[UserInfo].isSuccess)
  }

  it should "Return NOT FOUND error" in {
    receptionist ! GetUserProfile("invalid-id")
    expectMsgType[NotFoundError]
  }
}
