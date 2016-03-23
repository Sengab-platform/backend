package messages

object EnrollmentManagerMessages {

  trait EnrollmentMessage

  case class Enroll(projectId: Integer, userId: Integer) extends EnrollmentMessage

  case class Withdraw(projectId: Integer, userId: Integer) extends EnrollmentMessage

}
