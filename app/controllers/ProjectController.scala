package controllers

import java.util.concurrent.{TimeUnit, TimeoutException}
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import auth.services.AuthEnvironment
import messages.ProjectManagerMessages._
import models.Response
import models.errors.Error
import models.errors.GeneralErrors.{AskTimeoutError, BadJSONError}
import models.project.Project.NewProject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class ProjectController @Inject()(@Named("receptionist") receptionist: ActorRef)
                                 (override implicit val env: AuthEnvironment)
                                 (actorSystem: ActorSystem)
                                 (implicit exec: ExecutionContext) extends securesocial.core.SecureSocial {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  def index = Action {
    Ok("Welcome to Sengab")
  }


  //  list all projects (paginated)
  def listProjects(filter: String, offset: Int, limit: Int) = Action.async {
    receptionist ? ListProjects(filter, offset, limit) map {

      case Response(json) =>
        Ok(json)
      case error: Error =>
        error.result

    } recover {
      case e: TimeoutException =>
        AskTimeoutError("Failed to get projects",
          "Ask Timeout Exception on Actor Receptionist",
          this.getClass.toString).result
    }
  }

  //  get specific project
  def getProjectDetails(projectId: String, format: String) = Action.async {
    if (format.equals("with_template")) {
      receptionist ? GetProjectDetailsWithTemplateBody(projectId) map {
        case Response(json) =>
          Ok(json)
        case error: Error =>
          error.result
      } recover {
        case e: TimeoutException =>
          AskTimeoutError("Failed to get project details with template body",
            "Ask Timeout Exception on Actor Receptionist",
            this.getClass.toString).result
      }
    }
    else {
      receptionist ? GetProjectDetails(projectId) map {
        case Response(json) =>
          Ok(json)
        case error: Error =>
          error.result
      } recover {
        case e: TimeoutException =>
          AskTimeoutError("Failed to get project details",
            "Ask Timeout Exception on Actor Receptionist",
            this.getClass.toString).result
      }
    }
  }

  //  add project
  //  def addProject() = SecuredAction.async(BodyParsers.parse.json) {
  def addProject() = Action.async(BodyParsers.parse.json) {

    request => {
      // extract project item and the user ID from request
      val project = request.body.asOpt[NewProject]
      //      val userID = request.user.main.userId

      project match {
        //got Project Item
        case Some(project) =>
          receptionist ? CreateProject(project, s"user::117521628211683444029") map {
            // project created successfully
            case Response(json) =>
              Created(json)
            // failed to create project
            case error: Error =>
              error.result

          } recover {
            // timeout exception
            case e: TimeoutException =>
              AskTimeoutError("project creation failed",
                "Ask Timeout Exception on Actor Receptionist",
                this.getClass.toString).result

          }
        // could't parse Json and get Project Item
        case None =>
          Future(BadJSONError("project creation failed", "wrong JSON", this.getClass.toString).result)
      }
    }
  }

  //  search in projects (paginated)
  def searchProjects(keyword: String, offset: Int, limit: Int) = Action.async {
    receptionist ? SearchProjects(keyword, offset, limit) map {
      case Response(json) =>
        Ok(json)
      case error: Error =>
        error.result
    } recover {
      case e: TimeoutException =>
        AskTimeoutError("Failed to search for projects",
          "Ask Timeout Exception on Actor Receptionist",
          this.getClass.toString).result

    }
  }


  //  list stats of a project
  def getProjectStats(projectId: String) = Action.async {
    receptionist ? GetProjectStats(projectId) map {
      case Response(json) =>
        Ok(json)
      case error: Error =>
        error.result
    } recover {
      case e: TimeoutException =>
        AskTimeoutError("Failed to get project stats",
          "Ask Timeout Exception on Actor Receptionist",
          this.getClass.toString).result
    }
  }

  // list results of a project (paginated)
  def getProjectResults(projectId: String, offset: Int, limit: Int) = TODO


  //  Individuals Requests

  //  get more feed
  def getMoreFeed(projectId: String) = TODO
}
