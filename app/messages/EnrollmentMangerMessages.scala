package messages

object EnrollmentMangerMessages {

  trait EnrollmentMessage

  case class Enroll(projectId: Integer, userId: Integer) extends EnrollmentMessage

  case class Withdraw(projectId: Integer, userId: Integer) extends EnrollmentMessage

}
