package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.GetProjectDetails
import play.api.Logger

class ProjectRetriever extends Actor {
  override def receive = {
    case GetProjectDetails(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectDetails(projectID)} ")

      val projectDetailsRetriever = context.actorOf(ProjectDetailsRetriever.props(sender()), "projectDetailsRetriever")
      projectDetailsRetriever forward GetProjectDetails(projectID)


  }
}

object ProjectRetriever {
  def props(): Props = Props(new ProjectRetriever)
}

