package controllers

import java.util.concurrent.{TimeUnit, TimeoutException}
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import messages.ProjectManagerMessages.CreateProject
import models.project.Project
import models.responses.{Error, ErrorMsg, Response}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class ProjectController @Inject()(@Named("receptionist") receptionist: ActorRef)
                                 (actorSystem: ActorSystem)
                                 (implicit exec: ExecutionContext) extends Controller {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  def index = Action {
    Ok("Welcome to Sengab")
  }


  //  list all projects (paginated)
  def listProjects(filter: String, offset: Int, limit: Int) = Action {
    Ok
  }

  //  get specific project
  def getProject(projectId: String) = TODO

  //  add project
  def addProject() = Action.async(BodyParsers.parse.json) { request => {

    // try to extract project item from request
    val project = request.body.asOpt[Project]

    // try to extract project item from request
    project match {
      //got Project Item
      case Some(project) =>
        receptionist ? CreateProject(project, "user::567878") map {
          // project created successfully
          case Response(feed) =>
            Created(feed)
          // failed to create project
          case Error(result) =>
            result
        } recover {
          // timeout exception
          case e: TimeoutException =>
            BadRequest(ErrorMsg("project creation failed", "Ask Timeout Exception on Actor Receptionist").toJson)
        }
      // could't parse Json and get Project Item
      case None =>
        Future(BadRequest(ErrorMsg("project creation failed", "wrong JSON").toJson))
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
