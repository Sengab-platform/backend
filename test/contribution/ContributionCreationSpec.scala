package contribution

import core.AbstractSpec
import messages.ContributionManagerMessages.CreateContribution
import models.contribution.Contribution
import models.contribution.ContributionDataTypes._
import models.{Contributor, Response}

class ContributionCreationSpec extends AbstractSpec {

  // create contribution with 4 templates

  "Receptionist Actor" should "Create TypeOne Contribution Successfully" in {

    val contributionData = ContributionDataTypeOne(Location(4545, 454), "yes")

    val contribution = Contribution("project::5c785b0b-a7b6-4db0-99c1-c528814fb8e0",
      "2015",
      contributionData)

    val contributor = Contributor("user::117521628211683444029", "male")
    //    receptionist ! Enroll("user::117521628211683444029","project::5c785b0b-a7b6-4db0-99c1-c528814fb8e0")
    receptionist ! CreateContribution(contribution, contributor)
    val response = expectMsgType[Response]
    assert((response.jsonResult \ "id").validate[String].isSuccess)
    assert((response.jsonResult \ "url").validate[String].isSuccess)
  }


  "Receptionist Actor" should "Create TypeTwo Contribution Successfully" in {

    val contributionData = ContributionDataTypeTwo("dsdsdssdsd", "cat")

    val contribution = Contribution("project::5c785b0b-a7b6-4db0-99c1-c528814fb8e0",
      "2015",
      contributionData)

    val contributor = Contributor("user::117521628211683444029", "male")
    //    receptionist ! Enroll("user::117521628211683444029","project::5c785b0b-a7b6-4db0-99c1-c528814fb8e0")
    receptionist ! CreateContribution(contribution, contributor)
    val response = expectMsgType[Response]
    assert((response.jsonResult \ "id").validate[String].isSuccess)
    assert((response.jsonResult \ "url").validate[String].isSuccess)
  }

  "Receptionist Actor" should "Create TypeThree Contribution Successfully" in {

    val contributionData = ContributionDataTypeThree(Seq(Answer("1", "yes"), Answer("2", "no")))

    val contribution = Contribution("project::5c785b0b-a7b6-4db0-99c1-c528814fb8e0",
      "2015",
      contributionData)

    val contributor = Contributor("user::117521628211683444029", "male")
    //    receptionist ! Enroll("user::117521628211683444029","project::5c785b0b-a7b6-4db0-99c1-c528814fb8e0")
    receptionist ! CreateContribution(contribution, contributor)
    val response = expectMsgType[Response]
    assert((response.jsonResult \ "id").validate[String].isSuccess)
    assert((response.jsonResult \ "url").validate[String].isSuccess)
  }

  "Receptionist Actor" should "Create TypeFour Contribution Successfully" in {

    val contributionData = ContributionDataTypeFour("dsdsdssdsd", "cat", Location(4545, 87878))

    val contribution = Contribution("project::5c785b0b-a7b6-4db0-99c1-c528814fb8e0",
      "2015",
      contributionData)

    val contributor = Contributor("user::117521628211683444029", "male")
    //    receptionist ! Enroll("user::117521628211683444029","project::5c785b0b-a7b6-4db0-99c1-c528814fb8e0")
    receptionist ! CreateContribution(contribution, contributor)
    val response = expectMsgType[Response]
    assert((response.jsonResult \ "id").validate[String].isSuccess)
    assert((response.jsonResult \ "url").validate[String].isSuccess)
  }

  //
  //  // add contribution with un-enrolled user
  //
  //  "Receptionist Actor" should "Return Not Enrolled Error" in {
  //    val contributionData = ContributionDataTypeFour("dsdsdssdsd", "cat", Location("4545", "87878"))
  //
  //    val contribution = Contribution("project::5c785b0b-a7b6-4db0-99c1-c528814fb8e0",
  //      "2015",
  //      contributionData)
  //
  //    val contributor = Contributor("user::117521628211683444029", "male")
  //    receptionist ! Withdraw("user::117521628211683444029", "project::5c785b0b-a7b6-4db0-99c1-c528814fb8e0")
  //    receptionist ! CreateContribution(contribution, contributor)
  //    expectMsgType[Forbidden]
  //
  //
  //  }

  // todo Create contribution with invalid data should return error


}
