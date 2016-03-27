package messages

import models.Contribution


object ContributionManagerMessages {

  trait ContributionMessage

  case class SubmitContribution(contribution: Contribution, userID: String) extends ContributionMessage

  case class ValidateContribution(contribution: Contribution, userID: String) extends ContributionMessage

  case class CreateContribution(contribution: Contribution, userID: String) extends ContributionMessage

}
