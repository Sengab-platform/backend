package controllers

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class UserController @Inject()(@Named("receptionist") receptionist: ActorRef)
                              (actorSystem: ActorSystem)
                              (implicit exec: ExecutionContext) extends Controller {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  //  User Requests

  //  get user profile
  def getUSer(userId: String) = TODO

  //  list User’s Activity (paginated)
  def getUserActivities(userId: String, offset: Int, limit: Int) = TODO

  //  list all projects that the user enrolled in (paginated)
  def getUserEnrolledProjects(userId: String, offset: Int, limit: Int) = TODO

  //  list projects created by a specific user (paginated)
  def getUserCreatedProjects(userId: String, offset: Int, limit: Int) = TODO

}