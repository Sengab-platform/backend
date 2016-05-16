package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages._
import play.api.Logger

class ProjectRetriever extends Actor {
  override def receive = {
    case GetProjectDetails(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectDetails(projectID)} ")

      val projectDetailsRetriever = context.actorOf(ProjectDetailsRetriever.props(sender()), "projectDetailsRetriever")
      projectDetailsRetriever forward GetProjectDetails(projectID)

    case GetProjectDetailsWithTemplateBody(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectDetailsWithTemplateBody(projectID)} ")

      val projectDetailsWithTemplateBodyRetriever = context.actorOf(ProjectDetailsWithTemplateBodyRetriever.props(sender()), "projectDetailsWithTemplateBodyRetriever")
      projectDetailsWithTemplateBodyRetriever forward GetProjectDetailsWithTemplateBody(projectID)

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

    case GetProjectResults(projectID, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectResults(projectID, offset, limit)} ")

      val projectResultsRetriever = context.actorOf(ProjectResultsRetriever.props(sender()), "projectResultsRetriever")
      projectResultsRetriever forward GetProjectResults(projectID, offset, limit)
  }
}

object ProjectRetriever {
  def props(): Props = Props(new ProjectRetriever)
}

