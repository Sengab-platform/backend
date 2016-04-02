package messages

import models.project.NewProject

object ProjectManagerMessages {

  trait ProjectMessage

  case class CreateProject(project: NewProject, userID: String) extends ProjectMessage

  case class ListProjects(filter: String, offset: Integer, limit: Integer) extends ProjectMessage

  case class GetProjectDetails(projectID: String) extends ProjectMessage

  case class GetProjectResults(projectID: String, offset: Integer, limit: Integer) extends ProjectMessage

  case class GetProjectStats(projectID: String) extends ProjectMessage

  case class SearchProjects(keyword: String) extends ProjectMessage

  case class ValidateProject(project: NewProject, userID: String) extends ProjectMessage

}
