package actors.contribution

import akka.actor.{Actor, Props}
import messages.ContributionManagerMessages.{CreateContribution, ValidateContribution}
import models.contribution.Contribution
import play.api.Logger

class ContributionValidator extends Actor {
  override def receive = {
    case ValidateContribution(contribution, contributor) =>
      Logger.info(s"actor ${self.path} - received msg : ${ValidateContribution(contribution, contributor)}")

      val contributionCreator = context.actorOf(ContributionCreator.props(sender()))
      // check if a contribution is valid
      if (isValid(contribution)) contributionCreator forward CreateContribution(contribution, contributor)
      else {
        //        sender() ! Error(Results.BadRequest(ErrorMsg("project creation failed", "not valid project").toJson))
      }

  }

  // just place holder
  def isValid(c: Contribution) = true
}

object ContributionValidator {
  def props(): Props = Props(new ContributionValidator)
}