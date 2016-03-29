package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.GetProjectStats

class ProjectStatsRetriever extends Actor {
  override def receive = {
    case GetProjectStats(projectID) => ???
  }
}

object ProjectStatsRetriever {
  def props(): Props = Props(new ProjectStatsRetriever)
}
