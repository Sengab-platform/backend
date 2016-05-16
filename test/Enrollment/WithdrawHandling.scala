package Enrollment

import core.AbstractSpec
import messages.EnrollmentManagerMessages.Withdraw
import models.Response
import utils.Constants

class WithdrawHandling extends AbstractSpec {

  "Receptionist Actor" should "Enroll in a project Successfully" in {
    receptionist ! Withdraw(Constants.ValidUserID, Constants.AlreadyEnrolledProject)
    expectMsgType[Response]
  }

}
