package messages

import models.enrollment.Enrollment

object EnrollmentManagerMessages {

  trait EnrollmentMessage

  case class Enroll(enrollment: Enrollment) extends EnrollmentMessage

  case class Withdraw(withdraw: Enrollment) extends EnrollmentMessage

}
