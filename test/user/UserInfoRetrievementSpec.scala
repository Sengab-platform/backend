package user

import core.AbstractSpec
import messages.UserManagerMessages.GetUserProfile
import models.errors.GeneralErrors.NotFoundError
import models.{Response, UserInfo}
import utils.Constants

class UserInfoRetrievementSpec extends AbstractSpec {

  "Receptionist Actor" should "Get User Info Successfully" in {
    receptionist ! GetUserProfile(Constants.ValidUserID)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[UserInfo].isSuccess)
  }

  it should "Return NOT FOUND error" in {
    receptionist ! GetUserProfile(Constants.InvalidID)
    expectMsgType[NotFoundError]
  }
}
