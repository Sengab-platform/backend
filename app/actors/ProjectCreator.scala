package actors

import akka.actor.{Actor, Props}
import messages.ProjectMangerMessages.CreateProject

class ProjectCreator extends Actor {
  override def receive = {
    case CreateProject(project) => ???
  }
}

object ProjectCreator {
  def props(): Props = Props(new ProjectCreator)
}
