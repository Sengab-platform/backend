package actors.user

import akka.actor.{Actor, Props}
import messages.UserManagerMessages.ListProjectsOfUser

class ProjectsRetriever extends Actor {
  override def receive = {
    case ListProjectsOfUser(userId, sort, offset, limit) => ???
  }
}

object ProjectsRetriever {
  def props(): Props = Props(new ProjectsRetriever)
}
