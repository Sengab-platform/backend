package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.ValidateProject

class ProjectValidator extends Actor {
  override def receive = {
    case ValidateProject(project) => ???
  }
}

object ProjectValidator {
  def props(): Props = Props(new ProjectValidator)
}

