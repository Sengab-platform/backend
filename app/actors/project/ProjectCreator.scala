package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.CreateProject

class ProjectCreator extends Actor {
  override def receive = {
    case CreateProject(project, userID) => ???
  }
}

object ProjectCreator {
  def props(): Props = Props(new ProjectCreator)
}
