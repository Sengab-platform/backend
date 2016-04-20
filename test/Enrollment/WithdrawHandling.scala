package Enrollment

import core.AbstractSpec
import messages.EnrollmentManagerMessages.Withdraw
import models.Response

class WithdrawHandling extends AbstractSpec {

  "Receptionist Actor" should "Enroll in a project Successfully" in {
    receptionist ! Withdraw("user::117521628211683444029", "project::3")
    expectMsgType[Response]
  }

}
