package actors.enrollment

import akka.actor.{Actor, Props}
import messages.EnrollmentManagerMessages.Withdraw

class WithdrawHandler extends Actor {
  override def receive = {
    case Withdraw(projectId, userId) => ???
  }
}

object WithdrawHandler {
  def props(): Props = Props(new WithdrawHandler)
}