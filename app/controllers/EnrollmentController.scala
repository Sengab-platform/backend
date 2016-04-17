package controllers

import java.util.concurrent.{TimeUnit, TimeoutException}
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import auth.services.AuthEnvironment
import messages.EnrollmentManagerMessages.{Enroll, Withdraw}
import models.Response
import models.errors.Error
import models.errors.GeneralErrors.{AskTimeoutError, BadJSONError}
import play.api.mvc.{Action, BodyParsers}

import scala.concurrent.{ExecutionContext, Future}

class EnrollmentController @Inject()(@Named("receptionist") receptionist: ActorRef)
                                    (override implicit val env: AuthEnvironment)
                                    (actorSystem: ActorSystem)
                                    (implicit exec: ExecutionContext) extends securesocial.core.SecureSocial {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  //  Enrollment Requests

  //  enroll in a project
  def enrollInProject() = Action.async(BodyParsers.parse.json) {
    request => {
      val projectID = (request.body \ "project_id").asOpt[String]
      //val userID = request.user.main.userId

      projectID match {
        // got ProjectID
        case Some(projectID) =>
          receptionist ? Enroll(s"user::117521628211683444029", projectID) map {

            case Response(json) =>
              Created(json)

            // failed to enroll
            case err: Error =>
              err.result

        } recover {
          // timeout exception
          case e: TimeoutException =>
            AskTimeoutError("Enroll to project process failed", "Ask Timeout Exception on Actor Receptionist", this.getClass.toString).result
        }
        // could't parse Json and get projectID
      case None =>
        Future(BadJSONError("Enroll to project process failed", "wrong JSON", this.getClass.toString).result)
      }
    }
  }


  //  withdraw from project
  def WithdrawFromProject() = Action.async(BodyParsers.parse.json) {
    request => {
      val projectID = (request.body \ "project_id").asOpt[String]
      //val userID = request.user.main.userId

      projectID match {
        // got projectID
        case Some(projectID) =>
          receptionist ? Withdraw(s"user::117521628211683444029", projectID) map {

            case Response(json) =>
              Created(json)

            // failed to enroll
            case err: Error =>
              err.result

          } recover {
            // timeout exception
            case e: TimeoutException =>
              AskTimeoutError("Withdraw from project process failed", "Ask Timeout Exception on Actor Receptionist", this.getClass.toString).result
          }
        // could't parse Json and get projectID
        case None =>
          Future(BadJSONError("Withdraw from project process failed", "wrong JSON", this.getClass.toString).result)
      }
    }
  }
}