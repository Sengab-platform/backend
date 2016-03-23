package actors.enrollment

import akka.actor.{Actor, Props}
import messages.EnrollmentMangerMessages.Enroll

class EnrollmentHandler extends Actor {
  override def receive = {
    case Enroll(projectId, userId) => ???
  }
}

object EnrollmentHandler {
  def props(): Props = Props(new EnrollmentHandler)
}