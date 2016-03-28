package messages

import models.project.Project

object ProjectManagerMessages {

  trait ProjectMessage

  case class CreateProject(project: Project, userID: String) extends ProjectMessage

  case class ListProjects(filter: String, offset: Integer, limit: Integer) extends ProjectMessage

  case class GetProjectDetails(projectID: String) extends ProjectMessage

  case class GetProjectResults(projectID: String, offset: Integer, limit: Integer) extends ProjectMessage

  case class GetProjectStats(projectID: String) extends ProjectMessage

  case class SearchProjects(keyword: String) extends ProjectMessage

  case class ValidateProject(project: Project, userID: String) extends ProjectMessage

}
