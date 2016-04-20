package Enrollment

import core.AbstractSpec
import messages.EnrollmentManagerMessages.Enroll
import models.Response
import models.errors.GeneralErrors.{AlreadyExists, NotFoundError}

class EnrollmentHandling extends AbstractSpec {

  "Receptionist Actor" should "Enroll in a project Successfully" in {
    receptionist ! Enroll("user::117521628211683444029", "project::2")
    expectMsgType[Response]
  }

  it should "Return AlreadyExists error" in {
    receptionist ! Enroll("user::117521628211683444029", "project::1")
    expectMsgType[AlreadyExists]
  }

  it should "Return NOT FOUND error" in {
    receptionist ! Enroll("user::117521628211683444029", "project::1a1a")
    expectMsgType[NotFoundError]
  }
}
