package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages._

class ProjectManager extends Actor {
  override def receive = {
    case CreateProject(project) => ???

    case ListProjects(filter, offset, limit) => ???

    case GetProjectDetails(projectID) => ???

    case GetProjectResults(projectID, offset, limit) => ???

    case GetProjectStats(projectID) => ???

    case SearchProjects(keyword) => ???

  }
}

object ProjectManager {
  def props(): Props = Props(new ProjectManager)
}