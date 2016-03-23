package actors

import akka.actor.{Props, Actor}
import messages.ProjectMangerMessages.ProjectMessage


class Receptionist extends Actor {

  override def receive = {
    case msg: ProjectMessage =>

  }
}

object Receptionist {
  def props(): Props = Props(new Receptionist)
}