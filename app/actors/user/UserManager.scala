package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.{GetUserProfile, ListProjectsOfUser, ListUserActivity}
import play.Logger

class UserManager extends Actor {

  val userRetriever = context.actorOf(UserRetriever.props(), "userRetriever")

  override def receive = {

    case GetUserProfile(userID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetUserProfile(userID)} ")
      // forward message to UserRetriever
      userRetriever forward GetUserProfile(userID)

    case ListUserActivity(userID, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListUserActivity(userID, offset, limit)} ")
      // forward message to UserRetriever
      userRetriever forward ListUserActivity(userID, offset, limit)

    case ListProjectsOfUser(userID, sort, offset, limit) => ???
  }
}

object UserManager {
  def props(): Props = Props(new UserManager)
}

