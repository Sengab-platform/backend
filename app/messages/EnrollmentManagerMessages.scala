package messages

object EnrollmentManagerMessages {

  trait EnrollmentMessage

  case class Enroll(userID: String, projectID: String) extends EnrollmentMessage

  case class Withdraw(userID: String, projectID: String) extends EnrollmentMessage

}
