package actors.enrollment

import akka.actor.{Actor, Props}
import messages.EnrollmentManagerMessages.{Enroll, Withdraw}

class EnrollmentManager extends Actor {
  override def receive = {
    case Enroll(projectId, userId) => ???

    case Withdraw(projectId, userId) => ???
  }
}

object EnrollmentManager {
  def props(): Props = Props(new EnrollmentManager)
}
