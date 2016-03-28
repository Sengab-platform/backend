package controllers

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import messages.UserManagerMessages.{GetUserProfile, ListProjectsOfUser, ListUserActivity}
import models.responses._
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, TimeoutException}

class UserController @Inject()(@Named("receptionist") receptionist: ActorRef)
                              (actorSystem: ActorSystem)
                              (implicit exec: ExecutionContext) extends Controller {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  //  User Requests

  //  get user profile
  def getUser(userId: String) = Action.async {
    request => {
      // Ask receptionist to get user info
      receptionist ? GetUserProfile(userId) map {
        // The receptionist got the info
        //        case Response(feed) =>
        //          Ok(feed)
        // The receptionist failed to get user info
        case Error(result) =>
          result
      } recover {
        // timeout exception
        case e: TimeoutException =>
          BadRequest(ErrorMsg("user info retirement failed", "Ask Timeout Exception on Actor Receptionist").toJson)
      }
    }
  }


  //  list User’s Activity (paginated)
  def getUserActivities(userId: String, offset: Int, limit: Int) = Action.async {
    request => {
      // Ask receptionist to get user activates
      receptionist ? ListUserActivity(userId, offset, limit) map {
        // The receptionist got the activates
        //        case Response(feed) =>
        //          Ok(feed)
        // The receptionist failed to get user activates
        case Error(result) =>
          result
      } recover {
        // timeout exception
        case e: TimeoutException =>
          BadRequest(ErrorMsg("user activates retirement failed", "Ask Timeout Exception on Actor Receptionist").toJson)
      }
    }
  }

  //  list all projects that the user enrolled in (paginated)
  def getUserEnrolledProjects(userId: String, offset: Int, limit: Int) = Action.async {
    request => {
      val EnrolledSort = "enrolled"
      // Ask receptionist to get user enrolled projects
      receptionist ? ListProjectsOfUser(userId, EnrolledSort, offset, limit) map {
        // The receptionist got the activates
        //        case Response(feed) =>
        //          Ok(feed)
        // The receptionist failed to get user enrolled projects
        case Error(result) =>
          result
      } recover {
        // timeout exception
        case e: TimeoutException =>
          BadRequest(ErrorMsg("user enrolled projects retirement failed", "Ask Timeout Exception on Actor Receptionist").toJson)
      }
    }
  }

  //  list projects created by a specific user (paginated)
  def getUserCreatedProjects(userId: String, offset: Int, limit: Int) = Action.async {
    request => {
      val CreatedSort = "created"
      // Ask receptionist to get user created projects
      receptionist ? ListProjectsOfUser(userId, CreatedSort, offset, limit) map {
        // The receptionist got the activates
        //        case Response(feed) =>
        //          Ok(feed)
        // The receptionist failed to get user created projects
        case Error(result) =>
          result
      } recover {
        // timeout exception
        case e: TimeoutException =>
          BadRequest(ErrorMsg("user created projects retirement failed", "Ask Timeout Exception on Actor Receptionist").toJson)
      }
    }
  }
}