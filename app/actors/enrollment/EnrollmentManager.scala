package actors.enrollment

import akka.actor.{Actor, Props}
import messages.EnrollmentManagerMessages.{Enroll, Withdraw}

class EnrollmentManager extends Actor {
  override def receive = {
    case Enroll(projectID, userID) => ???

    case Withdraw(projectID, userID) => ???
  }
}

object EnrollmentManager {
  def props(): Props = Props(new EnrollmentManager)
}
