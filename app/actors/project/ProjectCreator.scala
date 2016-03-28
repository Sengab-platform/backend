package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.CreateProject
import models.responses.Response
import play.api.libs.json.Json

class ProjectCreator extends Actor {
  override def receive = {
    case CreateProject(project, userID) =>
      // create new project in the database
      sender() ! Response(Json.toJson("project added successfully"))
  }
}

object ProjectCreator {
  def props(): Props = Props(new ProjectCreator)
}
