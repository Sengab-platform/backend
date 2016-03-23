package actors

import akka.actor.{Actor, Props}
import messages.ProjectMangerMessages.GetProjectResults

class ResultsRetriever extends Actor {
  override def receive = {
    case GetProjectResults(projectID, offset, limit) => ???
  }
}

object ResultsRetriever {
  def props(): Props = Props(new ResultsRetriever)
}
