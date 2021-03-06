package messages

import models.project.Project.NewProject

object ProjectManagerMessages {

  trait ProjectMessage

  case class CreateProject(project: NewProject, userID: String) extends ProjectMessage

  case class ListProjects(filter: String, offset: Int, limit: Int) extends ProjectMessage

  case class GetProjectDetails(projectID: String) extends ProjectMessage

  case class GetProjectDetailsWithTemplateBody(projectID: String) extends ProjectMessage

  case class GetProjectResults(projectID: String, offset: Int, limit: Int) extends ProjectMessage

  case class GetProjectStats(projectID: String) extends ProjectMessage

  case class SearchProjects(keyword: String, offset: Int, limit: Int) extends ProjectMessage

  case class ValidateProject(project: NewProject, userID: String) extends ProjectMessage

}
