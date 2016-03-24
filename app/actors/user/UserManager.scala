package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.{GetUserProfile, ListProjectsOfUser, ListUserActivity}

class UserManager extends Actor {
  override def receive = {
    case GetUserProfile(userID) => ???

    case ListUserActivity(userID, offset, limit) => ???

    case ListProjectsOfUser(userID, sort, offset, limit) => ???

  }
}

object UserManager {
  def props(): Props = Props(new UserManager)
}

