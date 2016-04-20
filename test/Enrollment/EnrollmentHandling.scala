package Enrollment

import core.AbstractSpec
import messages.EnrollmentManagerMessages.Enroll
import models.Response
import models.errors.GeneralErrors.{AlreadyExists, NotFoundError}
import utils.Constants

class EnrollmentHandling extends AbstractSpec {

  "Receptionist Actor" should "Enroll in a project Successfully" in {
    receptionist ! Enroll(Constants.ValidUserID, Constants.ValidProjectToEnroll)
    expectMsgType[Response]
  }

  it should "Return AlreadyExists error" in {
    receptionist ! Enroll(Constants.ValidUserID, Constants.AlreadyEnrolledProject)
    expectMsgType[AlreadyExists]
  }

  it should "Return NOT FOUND error" in {
    receptionist ! Enroll(Constants.ValidUserID, Constants.InvalidID)
    expectMsgType[NotFoundError]
  }
}
