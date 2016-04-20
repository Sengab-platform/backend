package Enrollment

import core.AbstractSpec
import messages.EnrollmentManagerMessages.Enroll
import models.Response
import models.errors.GeneralErrors.AlreadyExists

class EnrollmentHandling extends AbstractSpec {

  "Receptionist Actor" should "Enroll in a project Successfully" in {
    receptionist ! Enroll("user::117521628211683444029", "project::2")
    expectMsgType[Response]
  }

  it should "Return AlreadyExists error" in {
    receptionist ! Enroll("user::117521628211683444029", "project::1")
    expectMsgType[AlreadyExists]
  }

  //todo add not found project test case (not implemented yet in DB)

}
