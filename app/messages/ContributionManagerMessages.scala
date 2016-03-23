package messages

import models.models.Contribution

object ContributionManagerMessages {

  trait ContributionMessage

  case class SubmitContribution(contribution: Contribution) extends ContributionMessage

  case class ValidateContribution(contribution: Contribution) extends ContributionMessage

  case class CreateContribution(contribution: Contribution) extends ContributionMessage

}
