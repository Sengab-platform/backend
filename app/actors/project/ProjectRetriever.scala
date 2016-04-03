package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.{GetProjectDetails, ListProjects}
import play.api.Logger

class ProjectRetriever extends Actor {
  override def receive = {
    case GetProjectDetails(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectDetails(projectID)} ")

      val projectDetailsRetriever = context.actorOf(ProjectDetailsRetriever.props(sender()), "projectDetailsRetriever")
      projectDetailsRetriever forward GetProjectDetails(projectID)


    case ListProjects(filter, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListProjects(filter, offset, limit)} ")

      val bulkProjectsRetriever = context.actorOf(BulkProjectsRetriever.props(sender()), "bulkProjectsRetriever")
      bulkProjectsRetriever forward ListProjects(filter, offset, limit)


  }
}

object ProjectRetriever {
  def props(): Props = Props(new ProjectRetriever)
}

