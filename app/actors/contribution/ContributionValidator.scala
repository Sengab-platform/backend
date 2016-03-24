package actors.contribution

import akka.actor.{Actor, Props}
import messages.ContributionManagerMessages.ValidateContribution

class ContributionValidator extends Actor {
  override def receive = {
    case ValidateContribution(contribution, userID) => ???
  }
}

object ContributionValidator {
  def props(): Props = Props(new ContributionValidator)
}