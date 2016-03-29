package actors.project

import akka.actor.Actor
import messages.ProjectManagerMessages.ListProjects

class BulkProjectsRetriever extends Actor {
  def receive = {
    case ListProjects(filter, offset, limit) => ???
  }
}
