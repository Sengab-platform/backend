package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.ListUserActivity
import models.responses.Response
import play.Logger
import play.api.libs.json.Json

class ActivityRetriever extends Actor {
  override def receive = {
    case ListUserActivity(userID, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListUserActivity(userID, offset, limit)} ")

      // Here we will send the result
      sender() ! Response(Json.toJson("user activates retrieved successfully"))

      // Kill activityRetriever
      context stop self

  }
}

object ActivityRetriever {
  def props(): Props = Props(new ActivityRetriever)
}
