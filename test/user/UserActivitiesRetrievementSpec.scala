package user

import core.AbstractSpec
import messages.UserManagerMessages.ListUserActivity
import models.errors.GeneralErrors.NotFoundError
import models.{Activities, Response}

class UserActivitiesRetrievementSpec extends AbstractSpec {

  //Test `ListUserActivity` message

  // If the user has some activities
  "Receptionist Actor" should "Get User Activities Successfully" in {
    // `ListUserActivity` message takes the ID of the activity, the trimmed ID of the user
    receptionist ! ListUserActivity("5", 0, 20)
    val response = expectMsgType[Response]
    // the response have to validate `Activities` model
    assert(response.jsonResult.validate[Seq[Activities]].isSuccess)
  }

  // If the user does not exist
  it should "Return NOT FOUND error" in {
    receptionist ! ListUserActivity("invalid-id", 0, 20)
    expectMsgType[NotFoundError]
  }
}
