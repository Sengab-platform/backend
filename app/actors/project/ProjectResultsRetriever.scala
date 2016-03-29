package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.GetProjectResults

class ProjectResultsRetriever extends Actor {
  override def receive = {
    case GetProjectResults(projectID, offset, limit) => ???
  }
}

object ProjectResultsRetriever {
  def props(): Props = Props(new ProjectResultsRetriever)
}
