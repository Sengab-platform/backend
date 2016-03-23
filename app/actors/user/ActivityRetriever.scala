package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.ListUserActivity

class ActivityRetriever extends Actor {
  override def receive = {
    case ListUserActivity(userId, offset, limit) => ???
  }
}

object ActivityRetriever {
  def props(): Props = Props(new ActivityRetriever)
}