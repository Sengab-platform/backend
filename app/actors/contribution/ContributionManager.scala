package actors.contribution

import akka.actor.{Actor, Props}
import messages.ContributionManagerMessages.{CreateContribution, ValidateContribution}
import play.api.Logger

class ContributionManager extends Actor {

  val contributionValidator = context.actorOf(ContributionValidator.props(), "contributionValidator")

  override def receive = {
    case CreateContribution(contribution, contributor) =>
      Logger.info(s"actor ${self.path} - received msg : ${CreateContribution(contribution, contributor)} ")

      contributionValidator forward ValidateContribution(contribution, contributor)
  }
}

object ContributionManager {
  def props(): Props = Props(new ContributionManager)
}
