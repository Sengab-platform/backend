package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.{GetProjectDetails, GetProjectStats, ListProjects, SearchProjects}
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

    case SearchProjects(keyword, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${SearchProjects(keyword, offset, limit)} ")

      val projectsSearchRetriever = context.actorOf(ProjectsSearchRetriever.props(sender()), "projectsSearchRetriever")
      projectsSearchRetriever forward SearchProjects(keyword, offset, limit)

    case GetProjectStats(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectStats(projectID)} ")

      val projectStatsRetriever = context.actorOf(ProjectStatsRetriever.props(sender()), "projectStatsRetriever")
      projectStatsRetriever forward GetProjectStats(projectID)

  }
}

object ProjectRetriever {
  def props(): Props = Props(new ProjectRetriever)
}

