package messages

object EnrollmentManagerMessages {

  trait EnrollmentMessage

  case class Enroll(projectId: String, userId: String) extends EnrollmentMessage

  case class Withdraw(projectId: String, userId: String) extends EnrollmentMessage

}
