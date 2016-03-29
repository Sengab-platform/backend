package actors.enrollment

import akka.actor.{Actor, ActorRef, Props}
import messages.EnrollmentManagerMessages.Enroll

class EnrollmentHandler(out: ActorRef) extends Actor {
  override def receive = {
    case Enroll(enrollment) => ???
  }
}

object EnrollmentHandler {
  def props(out: ActorRef): Props = Props(new EnrollmentHandler(out: ActorRef))
}