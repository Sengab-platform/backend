package controllers

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import models.project.Project
import models.project.Templates.TemplateFour
import models.responses.{Error, Response}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ProjectController @Inject()(@Named("receptionist") receptionist: ActorRef)
                                 (actorSystem: ActorSystem)
                                 (implicit exec: ExecutionContext) extends Controller {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  def index = Action {
    Ok("Welcome to Sengab")
  }


  //  list all projects (paginated)
  def listProjects(filter: String, offset: Int, limit: Int) = Action.async {
    val future = receptionist ? "Run" map {
      case Response(feed) =>
        BadRequest(feed)
      case Error(res) =>
        res
    }
    future
  }


  //  get specific project
  def getProject(projectId: String) = TODO

  //  add project
  def addProject() = Action(BodyParsers.parse.json) { request => {

    val project = request.body.asOpt[Project]
    project match {
      case Some(project) =>
        Ok(s"added ${project.name}")
      case None =>
        val p = Project("Reco", "short", "long", 500, 4, TemplateFour("Take a photo of road accidents you witness"))
        BadRequest(Json.toJson(p))
    }
  }
  }

  //  search in projects (paginated)
  def searchProjects(keyword: String, offset: Int, limit: Int) = TODO


  //  list stats of a project
  def getProjectStats(projectId: String) = TODO

  // list results of a project (paginated)
  def getProjectResults(projectId: String, offset: Int, limit: Int) = TODO


  //  Individuals Requests

  //  get more feed
  def getMoreFeed(projectId: String) = TODO
}
