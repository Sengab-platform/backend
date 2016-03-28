package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.GetUserProfile
import play.Logger

class InfoRetriever extends Actor {
  override def receive = {

    case GetUserProfile(userID) =>

      Logger.info(s"actor ${self.path} - received msg : ${GetUserProfile(userID)} ")

      // Here we will send the result
      //      sender() ! Response(Json.toJson("user info retrieved successfully"))

      // Kill infoRetriever
      context stop self

  }
}

object InfoRetriever {
  def props(): Props = Props(new InfoRetriever)
}
