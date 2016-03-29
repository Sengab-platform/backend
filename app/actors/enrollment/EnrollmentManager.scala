package actors.enrollment

import akka.actor.{Actor, Props}
import messages.EnrollmentManagerMessages.{Enroll, Withdraw}
import play.api.Logger

class EnrollmentManager extends Actor {
  override def receive = {
    case Enroll(enrollment) =>
      Logger.info(s"actor ${self.path} - received msg : ${Enroll(enrollment)}")
      val enrollmentHandler = context.actorOf(EnrollmentHandler.props(sender()), "enrollmentHandler")
      enrollmentHandler forward Enroll(enrollment)

    case Withdraw(withdraw) =>
      Logger.info(s"actor ${self.path} - received msg : ${Withdraw(withdraw)}")
      val withdrawHandler = context.actorOf(WithdrawHandler.props(sender()), "withdrawHandler")
      withdrawHandler forward Withdraw(withdraw)
  }
}

object EnrollmentManager {
  def props(): Props = Props(new EnrollmentManager)
}
