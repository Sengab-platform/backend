package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.{CreateProject, ValidateProject}
import models.project.NewProject
import play.api.Logger

class ProjectValidator extends Actor {


  override def receive = {
    case ValidateProject(project, userID) =>
      Logger.info(s"actor ${self.path} - received msg : ${ValidateProject(project, userID)} ")

      val projectCreator = context.actorOf(ProjectCreator.props(sender()), "projectCreator")
      // check if a project is valid
      if (isValid(project)) projectCreator forward CreateProject(project, userID)
      else {
        //        sender() ! Error(Results.BadRequest(ErrorMsg("project creation failed", "not valid project").toJson))
      }
  }

  // just place holder
  def isValid(p: NewProject) = true
}

object ProjectValidator {
  def props(): Props = Props(new ProjectValidator)
}

