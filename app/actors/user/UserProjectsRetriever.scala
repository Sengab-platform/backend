package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.ListProjectsOfUser
import play.Logger

class UserProjectsRetriever extends Actor {
  override def receive = {
    case ListProjectsOfUser(userID, sort, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListProjectsOfUser(userID, sort, offset, limit)} ")

      // Here we will send the result
      //      sender() ! Response(Json.toJson("user " + sort + " projects retrieved successfully"))

      // Kill userProjectsRetriever
      context stop self

  }
}

object UserProjectsRetriever {
  def props(): Props = Props(new UserProjectsRetriever)
}
