package project

import core.AbstractSpec
import messages.UserManagerMessages.ListUserActivity
import models.{Activities, Response}

class UserActivitiesRetrievementSpec extends AbstractSpec {

  //Test `ListUserActivity` message
  "Receptionist Actor" should "Get User Activities Successfully" in {
    // `ListUserActivity` message takes the ID of the activity, the trimmed ID of the user
    receptionist ! ListUserActivity("117521628211683444029", 0, 20)
    val response = expectMsgType[Response]
    // If the user has some activities, then the response will validate `Activities` model
    assert(response.jsonResult.validate[Activities].isSuccess
      // if not it will be equal to an empty array
      //todo may be there is a better way than converting to string
      || response.jsonResult.toString == "[]")
  }

}
