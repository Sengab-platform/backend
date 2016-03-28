package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.{CreateProject, ValidateProject}
import models.responses.{Error, ErrorMsg}
import play.api.Logger
import play.api.mvc.Results

class ProjectValidator extends Actor {

  val projectCreator = context.actorOf(ProjectCreator.props(), "projectCreator")

  // just place holder
  val valid = true

  override def receive = {
    case ValidateProject(project, userID) =>
      Logger.info(s"actor ${self.path} - received msg : ${ValidateProject(project, userID)} ")
      // check if a project is valid
      if (valid) projectCreator forward CreateProject(project, userID)
      else {
        sender() ! Error(Results.BadRequest(ErrorMsg("project creation failed", "not valid project").toJson))
      }
  }
}

object ProjectValidator {
  def props(): Props = Props(new ProjectValidator)
}

