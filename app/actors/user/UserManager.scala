package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.{GetUserProfile, ListProjectsOfUser, ListUserActivity}

class UserManager extends Actor {
  override def receive = {
    case GetUserProfile(userId) => ???

    case ListUserActivity(userId, offset, limit) => ???

    case ListProjectsOfUser(userId, sort, offset, limit) => ???

  }
}

object UserManager {
  def props(): Props = Props(new UserManager)
}

