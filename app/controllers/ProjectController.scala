package controllers

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ProjectController @Inject()(@Named("receptionist") receptionist: ActorRef)
                                 (actorSystem: ActorSystem)
                                 (implicit exec: ExecutionContext) extends Controller {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  def index = Action {
    Ok("Welcome to Sengab")
  }

  //  Project Requests

  //  list all projects (paginated)
  def listProjects(filter: String, offset: Int, limit: Int) = Action.async {
    val future = receptionist ? "Run" map {
      case "Success" =>
        Ok("listed")
      case "Failed" =>
        BadRequest("")
    }
    future
  }


  //  get specific project
  def getProject(projectId: String) = TODO

  //  add project
  def addProject() = TODO

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
