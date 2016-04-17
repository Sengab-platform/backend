package messages

import models.Contributor
import models.contribution.Contribution


object ContributionManagerMessages {

  trait ContributionMessage

  case class ValidateContribution(contribution: Contribution, contributor: Contributor) extends ContributionMessage

  case class CreateContribution(contribution: Contribution, contributor: Contributor) extends ContributionMessage

}
