package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.GetUserProfile

class InfoRetriever extends Actor {
  override def receive = {
    case GetUserProfile(userId) => ???
  }
}

object InfoRetriever {
  def props(): Props = Props(new InfoRetriever)
}
