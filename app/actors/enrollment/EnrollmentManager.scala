package actors.enrollment

import akka.actor.{Actor, Props}
import messages.EnrollmentManagerMessages.{Enroll, Withdraw}

class EnrollmentManager extends Actor {
  override def receive = {
    case Enroll(enrollment) => ???

    case Withdraw(enrollment) => ???
  }
}

object EnrollmentManager {
  def props(): Props = Props(new EnrollmentManager)
}
