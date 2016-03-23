package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.GetProjectStats

class StatsRetriever extends Actor {
  override def receive = {
    case GetProjectStats(projectID) => ???
  }
}

object StatsRetriever {
  def props(): Props = Props(new StatsRetriever)
}
