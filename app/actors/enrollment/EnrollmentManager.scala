package actors.enrollment

import akka.actor.{Actor, Props}
import messages.EnrollmentManagerMessages.{Enroll, Withdraw}
import models.Enrollment
import play.api.Logger

class EnrollmentManager extends Actor {
  override def receive = {
    case Enroll(userID: String, projectID: Enrollment) =>
      Logger.info(s"actor ${self.path} - received msg : ${Enroll(userID: String, projectID: Enrollment)}")
      val enrollmentHandler = context.actorOf(EnrollmentHandler.props(sender()))
      enrollmentHandler forward Enroll(userID: String, projectID: Enrollment)

    case Withdraw(userID: String, projectID: Enrollment) =>
      Logger.info(s"actor ${self.path} - received msg : ${Withdraw(userID: String, projectID: Enrollment)}")
      val withdrawHandler = context.actorOf(WithdrawHandler.props(sender()))
      withdrawHandler forward Withdraw(userID: String, projectID: Enrollment)
  }
}

object EnrollmentManager {
  def props(): Props = Props(new EnrollmentManager)
}
