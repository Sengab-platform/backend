package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.ListProjectsOfUser

class UserProjectsRetriever extends Actor {
  override def receive = {
    case ListProjectsOfUser(userID, sort, offset, limit) => ???
  }
}

object UserProjectsRetriever {
  def props(): Props = Props(new UserProjectsRetriever)
}
