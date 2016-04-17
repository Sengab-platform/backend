package messages

import models.Enrollment

object EnrollmentManagerMessages {

  trait EnrollmentMessage

  case class Enroll(userID: String, projectID: Enrollment) extends EnrollmentMessage

  case class Withdraw(userID: String, projectID: Enrollment) extends EnrollmentMessage

}
