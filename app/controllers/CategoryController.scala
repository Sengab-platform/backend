package controllers

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class CategoryController @Inject()(@Named("receptionist") receptionist: ActorRef)
                                  (actorSystem: ActorSystem)
                                  (implicit exec: ExecutionContext) extends Controller {
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)


  // Category Requests

  //  list all categories
  def getCategories(offset: Int, limit: Int) = TODO

  //  list projects of a category (paginated)
  def getProjectsForCategory(categoryId: String, offset: Int, limit: Int) = TODO


}