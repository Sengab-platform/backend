package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectMangerMessages.{GetProjectDetails, GetProjectResults, GetProjectStats}

class ProjectsRetriever extends Actor {
  override def receive = {
    case GetProjectDetails(projectID) => ???

    case GetProjectResults(projectID, offset, limit) => ???

    case GetProjectStats(projectID) => ???

  }
}

object ProjectsRetriever {
  def props(): Props = Props(new ProjectsRetriever)
}

