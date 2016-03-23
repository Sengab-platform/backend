package actors.project

import akka.actor.{Actor, Props}
import messages.ProjectManagerMessages.GetProjectResults

class ResultsRetriever extends Actor {
  override def receive = {
    case GetProjectResults(projectID, offset, limit) => ???
  }
}

object ResultsRetriever {
  def props(): Props = Props(new ResultsRetriever)
}
