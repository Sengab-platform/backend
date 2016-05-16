package actors.enrollment

import akka.actor.{Actor, Props}
import messages.EnrollmentManagerMessages.{Enroll, Withdraw}
import play.api.Logger

class EnrollmentManager extends Actor {
  override def receive = {
    case Enroll(userID: String, projectID: String) =>
      Logger.info(s"actor ${self.path} - received msg : ${Enroll(userID: String, projectID: String)}")
      val enrollmentHandler = context.actorOf(EnrollmentHandler.props(sender()), "enrollmentHandler")
      enrollmentHandler forward Enroll(userID: String, projectID: String)

    case Withdraw(userID: String, projectID: String) =>
      Logger.info(s"actor ${self.path} - received msg : ${Withdraw(userID: String, projectID: String)}")
      val withdrawHandler = context.actorOf(WithdrawHandler.props(sender()), "withdrawHandler")
      withdrawHandler forward Withdraw(userID: String, projectID: String)
  }
}

object EnrollmentManager {
  def props(): Props = Props(new EnrollmentManager)
}
