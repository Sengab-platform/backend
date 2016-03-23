package actors

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.ProjectMessage


class Receptionist extends Actor {

  override def receive = {
    case msg: ProjectMessage =>

  }
}

object Receptionist {
  def props(): Props = Props(new Receptionist)
}