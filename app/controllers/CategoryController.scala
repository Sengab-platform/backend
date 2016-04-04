package controllers

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import messages.CategoryManagerMessages.RetrieveCategories
import models.Response
import models.errors.Error
import models.errors.GeneralErrors.AskTimeoutError
import play.api.mvc.{Action, Controller}

import scala.concurrent._

class CategoryController @Inject()(@Named("receptionist") receptionist: ActorRef)
                                  (actorSystem: ActorSystem)
                                  (implicit exec: ExecutionContext) extends Controller {
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  // Category Requests

  //  list all categories
  def getCategories(offset: Int, limit: Int) = Action.async {
    request => {
      // Ask receptionist to get categories
      receptionist ? RetrieveCategories(offset, limit) map {

        // The receptionist got the categories
        case Response(response) =>
          Ok(response)

        // The receptionist failed to get the categories
        case error: Error =>
          error.result

      } recover {
        // timeout exception
        case e: TimeoutException =>
          AskTimeoutError("Retrieve Categories failed",
            "Ask Timeout Exception on Actor Receptionist", this.getClass.toString).result
      }
    }
  }

  //  list projects of a category
  def getProjectsForCategory(categoryID: String, offset: Int, limit: Int) = ???
}