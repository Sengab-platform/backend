package actors.enrollment

import akka.actor.{Actor, ActorRef, Props}
import messages.EnrollmentManagerMessages.Withdraw

class WithdrawHandler(out: ActorRef) extends Actor {
  override def receive = {
    case Withdraw(enrollment) => ???
  }
}

object WithdrawHandler {
  def props(out: ActorRef): Props = Props(new WithdrawHandler(out: ActorRef))
}