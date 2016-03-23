package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.{GetProjectDetails, ListProjects, SearchProjects}

class DetailsRetriever extends Actor {
  override def receive = {
    case ListProjects(filter, offset, limit) => ???

    case GetProjectDetails(projectID) => ???

    case SearchProjects(keyword) => ???
  }
}

object DetailsRetriever {
  def props(): Props = Props(new DetailsRetriever)
}