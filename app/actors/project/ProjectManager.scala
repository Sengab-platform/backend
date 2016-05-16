package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages._
import play.api.Logger

class ProjectManager extends Actor {

  val projectValidator = context.actorOf(ProjectValidator.props(), "projectValidator")
  val projectRetriever = context.actorOf(ProjectRetriever.props(), "projectRetriever")

  override def receive = {
    case CreateProject(project, userID) =>
      Logger.info(s"actor ${self.path} - received msg : ${CreateProject(project, userID)} ")
      projectValidator forward ValidateProject(project, userID)

    case ListProjects(filter, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListProjects(filter, offset, limit)} ")
      projectRetriever forward ListProjects(filter, offset, limit)


    case GetProjectDetails(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectDetails(projectID)} ")
      projectRetriever forward GetProjectDetails(projectID)

    case GetProjectDetailsWithTemplateBody(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectDetailsWithTemplateBody(projectID)} ")
      projectRetriever forward GetProjectDetailsWithTemplateBody(projectID)

    case GetProjectResults(projectID, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectResults(projectID, offset, limit)} ")
      projectRetriever forward GetProjectResults(projectID, offset, limit)

    case GetProjectStats(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectStats(projectID)} ")
      projectRetriever forward GetProjectStats(projectID)

    case SearchProjects(keyword, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${SearchProjects(keyword, offset, limit)} ")
      projectRetriever forward SearchProjects(keyword, offset, limit)

  }
}

object ProjectManager {
  def props(): Props = Props(new ProjectManager)
}