package actors.contribution

import akka.actor.{Actor, Props}
import messages.ContributionMangerMessages.SubmitContribution

class ContributionManager extends Actor {
  override def receive = {
    case SubmitContribution(contribution) => ???
  }
}

object ContributionManager {
  def props(): Props = Props(new ContributionManager)
}
