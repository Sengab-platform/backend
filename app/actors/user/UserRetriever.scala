package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.{GetUserProfile, ListProjectsOfUser, ListUserActivity}
import play.Logger

class UserRetriever extends Actor {

  override def receive = {

    case GetUserProfile(userID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetUserProfile(userID)} ")

      // Create InfoRetriever Actor
      val infoRetriever = context.actorOf(InfoRetriever.props(), "infoRetriever")

      // Forward GetUserProfile message to InfoRetriever actor
      infoRetriever forward GetUserProfile(userID)

    case ListUserActivity(userID, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListUserActivity(userID, offset, limit)} ")

      // Create ActivityRetriever Actor
      val activityRetriever = context.actorOf(ActivityRetriever.props())
      // Forward ListUserActivity message to ActivityRetriever actor
      activityRetriever forward ListUserActivity(userID, offset, limit)

    case ListProjectsOfUser(userID, sort, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListProjectsOfUser(userID, sort, offset, limit)} ")

      // Create ActivityRetriever Actor
      val userProjectsRetriever = context.actorOf(UserProjectsRetriever.props())
      // Forward ListUserActivity message to ActivityRetriever actor
      userProjectsRetriever forward ListProjectsOfUser(userID, sort, offset, limit)

  }
}

object UserRetriever {
  def props(): Props = Props(new UserRetriever)
}
