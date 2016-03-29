package actors.project

import akka.actor.Actor
import messages.ProjectManagerMessages.SearchProjects

class ProjectsSearchRetriever extends Actor {

  def receive = {
    case SearchProjects(keyword) => ???

  }
}
