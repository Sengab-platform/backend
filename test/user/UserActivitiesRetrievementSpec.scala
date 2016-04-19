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

  // If the user does not have activities
  it should "Get empty json array" in {
    receptionist ! ListUserActivity("117521628211683444029", 0, 20)
    val response = expectMsgType[Response]
    // the response will be an empty json array
    assert(response.jsonResult.toString == "[]")
  }
}
