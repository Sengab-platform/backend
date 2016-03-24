package messages

object EnrollmentManagerMessages {

  trait EnrollmentMessage

  case class Enroll(projectID: String, userId: String) extends EnrollmentMessage

  case class Withdraw(projectID: String, userId: String) extends EnrollmentMessage

}
