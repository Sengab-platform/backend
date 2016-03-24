package actors.enrollment

import akka.actor.{Actor, Props}
import messages.EnrollmentManagerMessages.Enroll

class EnrollmentHandler extends Actor {
  override def receive = {
    case Enroll(projectID, userID) => ???
  }
}

object EnrollmentHandler {
  def props(): Props = Props(new EnrollmentHandler)
}
