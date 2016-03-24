package messages

object EnrollmentManagerMessages {

  trait EnrollmentMessage

  case class Enroll(projectID: String, userID: String) extends EnrollmentMessage

  case class Withdraw(projectID: String, userID: String) extends EnrollmentMessage

}
