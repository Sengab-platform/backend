package controllers

import java.util.concurrent.{TimeUnit, TimeoutException}
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import messages.ContributionManagerMessages.CreateContribution
import models.contribution.Contribution
import models.errors.Error
import models.errors.GeneralErrors.{AskTimeoutError, BadJSONError}
import models.{Contributor, Response}
import play.api.mvc.{Action, BodyParsers, Controller}

import scala.concurrent.{ExecutionContext, Future}

class ContributionController @Inject()(@Named("receptionist") receptionist: ActorRef)
                                      (actorSystem: ActorSystem)
                                      (implicit exec: ExecutionContext) extends Controller {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)


  //  Contribution && Requests

  //  submit a contribution in a project
  // TODO make it secure
  def contributeInProject() = Action.async(BodyParsers.parse.json) {
    request => {
      // just mock data for now
      val contributor = Contributor("user::117521628211683444029", "male")
      val contribution = request.body.asOpt[Contribution]

      contribution match {
        case Some(c) =>
          receptionist ? CreateContribution(c, contributor) map {

            case Response(json) =>
              Ok(json)
            case error: Error =>
              error.result

          } recover {
            case e: TimeoutException =>
              AskTimeoutError("Failed to create contributions",
                "Ask Timeout Exception on Actor Receptionist",
                this.getClass.toString).result
          }
        case None =>
          Future(BadJSONError("contribution creation failed", "wrong JSON", this.getClass.toString).result)
      }

    }
  }
}