package controllers

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class ContributionController @Inject()(@Named("receptionist") receptionist: ActorRef)
                                      (actorSystem: ActorSystem)
                                      (implicit exec: ExecutionContext) extends Controller {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)


  //  Contribution && Requests

  //  submit a contribution in a project
  def contributeInProject() = TODO
}