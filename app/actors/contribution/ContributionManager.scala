package actors.contribution

import akka.actor.{Actor, Props}
import messages.ContributionManagerMessages.SubmitContribution

class ContributionManager extends Actor {
  override def receive = {
    case SubmitContribution(contribution, userID) => ???
  }
}

object ContributionManager {
  def props(): Props = Props(new ContributionManager)
}
