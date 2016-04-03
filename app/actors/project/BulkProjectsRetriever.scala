package actors.project

import akka.actor.{Actor, ActorRef, Props}
import messages.ProjectManagerMessages.ListProjects

class BulkProjectsRetriever(out: ActorRef) extends Actor {

  def receive = {
    case ListProjects(filter, offset, limit) => ???
  }
}

object BulkProjectsRetriever {
  def props(out: ActorRef): Props = Props(new BulkProjectsRetriever(out))
}
