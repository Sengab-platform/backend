package controllers

import java.util.concurrent.{TimeUnit, TimeoutException}
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import messages.EnrollmentManagerMessages.{Enroll, Withdraw}
import models.enrollment.Enrollment
import models.errors.Error
import models.errors.GeneralErrors.{AskTimeoutError, CouldNotParseJSON}
import play.api.mvc.{Action, BodyParsers, Controller}

import scala.concurrent.{ExecutionContext, Future}

class EnrollmentController @Inject()(@Named("receptionist") receptionist: ActorRef)
                                    (actorSystem: ActorSystem)
                                    (implicit exec: ExecutionContext) extends Controller {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  //  Enrollment Requests

  //  enroll in a project
  def enrollInProject() = Action.async(BodyParsers.parse.json) { request => {
    // extract enrollment keys/values from request
    val enrollment = request.body.asOpt[Enrollment]

    // extract enrollment keys/values from request
    enrollment match {
      // got enrollment keys/values
      case Some(enrollment) =>
        receptionist ? Enroll(enrollment) map {

          // user enrolled in project successfully
          // TODO fix this :

          //          case msg: EnrollResponse =>
          //            Created(Json.toJson(msg))

          // failed to enroll
          case err: Error =>
            err.result

        } recover {
          // timeout exception
          case e: TimeoutException =>
            AskTimeoutError("Enroll to project process failed", "Ask Timeout Exception on Actor Receptionist", this.getClass.toString).result
        }
      // could't parse Json and get enrollment keys/values
      case None =>
        Future(CouldNotParseJSON("Enroll to project process failed", "wrong JSON", this.getClass.toString).result)
    }
  }
  }


  //  withdraw from project
  def WithdrawFromProject() = Action.async(BodyParsers.parse.json) { request => {

    // extract withdraw keys/values from request
    val withdraw = request.body.asOpt[Enrollment]

    // extract withdraw keys/values from request
    withdraw match {
      // got withdraw keys/values
      case Some(withdraw) =>
        receptionist ? Withdraw(withdraw) map {

          // user has withdrawn from project successfully
          // TODO fix this :
          //          case msg: WithdrawResponse =>
          //            Ok(Json.toJson(msg))

          // failed to withdraw
          case err: Error =>
            err.result

        } recover {
          // timeout exception
          case e: TimeoutException =>
            AskTimeoutError("Withdraw from project process failed", "Ask Timeout Exception on Actor Receptionist", this.getClass.toString).result
        }
      // could't parse Json and get withdraw keys/values
      case None =>
        Future(CouldNotParseJSON("Withdraw from project failed", "wrong JSON", this.getClass.toString).result)

    }
  }
  }
}