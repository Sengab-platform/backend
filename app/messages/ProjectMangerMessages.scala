package messages

import models.models.Project

object ProjectMangerMessages {

  trait ProjectMessage

  case class CreateProject(project: Project) extends ProjectMessage

  case class ListProjects(filter: String, offset: Integer, limit: Integer) extends ProjectMessage

  case class GetProjectDetails(projectID: Integer) extends ProjectMessage

  case class GetProjectResults(projectID: Integer, offset: Integer, limit: Integer) extends ProjectMessage

  case class GetProjectStats(projectID: Integer) extends ProjectMessage

  case class SearchProjects(keyword: String) extends ProjectMessage

  case class ValidateProject(project: Project) extends ProjectMessage

}
