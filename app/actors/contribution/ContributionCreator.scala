package actors.contribution

import akka.actor.{Actor, Props}
import messages.ContributionManagerMessages.CreateContribution

class ContributionCreator extends Actor {
  override def receive = {
    case CreateContribution(contribution) => ???
  }
}

object ContributionCreator {
  def props(): Props = Props(new ContributionCreator)
}